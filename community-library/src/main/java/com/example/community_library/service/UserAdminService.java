package com.example.community_library.service;

import com.example.community_library.dto.CreateUserRequest;
import com.example.community_library.entity.Member;
import com.example.community_library.repository.MemberRepository;
import com.example.community_library.security.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Creates LIBRARIAN / MEMBER logins.
 * 1. Keycloak: user + default password (temporary) + realm role (roles stay in Keycloak, the app reads them from the JWT).
 * 2. Library DB: a row in the members table with the same role, the Keycloak user id and
 *    the time the default password stops working.
 * If step 2 fails, the Keycloak user from step 1 is removed again, so both stay in sync.
 *
 * Every new user gets the same default password (intern project testing). It must be changed at the
 * first login, and it is only valid for a short time (120 seconds by default) - see expireUnusedPasswords().
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminService {

    // Roles an ADMIN can create from the app (a new ADMIN is still made in the Keycloak console)
    public static final Set<String> CREATABLE_ROLES = Set.of("LIBRARIAN", "MEMBER");

    // Set DEFAULT_USER_PASSWORD in .env to change it
    @Value("${app.keycloak.default-user-password:Welcome@123}")
    private String defaultUserPassword;

    // How long the default password works before it must have been changed (TEMP_PASSWORD_VALIDITY_SECONDS)
    @Value("${app.keycloak.temp-password-validity-seconds:120}")
    private long tempPasswordValiditySeconds;

    private final KeycloakAdminService keycloakAdminService;
    private final MemberRepository memberRepository;

    /** Result of a create / reset: the row, the Keycloak login name, the default password and its expiry. */
    public record CreatedUser(Member member, String username, String temporaryPassword, OffsetDateTime expiresAt) {
    }

    public CreatedUser createUser(CreateUserRequest request) {
        String role = normalizeRole(request.getRole());
        String email = required(request.getEmail(), "email");
        String firstName = required(request.getFirstName(), "firstName");
        String lastName = request.getLastName() == null ? "" : request.getLastName().trim();
        String username = (request.getUsername() == null || request.getUsername().isBlank()
                ? email : request.getUsername().trim()).toLowerCase(Locale.ROOT);

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw badRequest("email is not valid");
        }
        if (memberRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A member with this email already exists");
        }

        // 1. Keycloak - default password, temporary = must be changed at the first login
        String keycloakUserId = keycloakAdminService.createUser(
                username, email, firstName, lastName, defaultUserPassword, true, role);

        // 2. Library DB - undo step 1 if this fails
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tempPasswordValiditySeconds);
        try {
            Member member = new Member();
            member.setName((firstName + " " + lastName).trim());
            member.setEmail(email);
            member.setPhone(request.getPhone());
            member.setAge(request.getAge());
            member.setRole(role);
            member.setKeycloakUserId(keycloakUserId);
            member.setTempPasswordExpiresAt(expiresAt);
            return new CreatedUser(memberRepository.save(member), username, defaultUserPassword, withZone(expiresAt));
        } catch (RuntimeException e) {
            keycloakAdminService.deleteUser(keycloakUserId);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Could not save the user in the library database; the Keycloak user was removed");
        }
    }

    /** ADMIN gives the default password again (e.g. it expired before the user logged in). */
    public CreatedUser resetToDefaultPassword(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        if (member.getKeycloakUserId() == null) {
            throw badRequest("This member has no Keycloak login");
        }
        if ("ADMIN".equalsIgnoreCase(member.getRole())) {
            throw badRequest("ADMIN passwords are managed in the Keycloak console");
        }

        keycloakAdminService.setPassword(member.getKeycloakUserId(), defaultUserPassword, true);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tempPasswordValiditySeconds);
        member.setTempPasswordExpiresAt(expiresAt);
        memberRepository.save(member);
        return new CreatedUser(member, null, defaultUserPassword, withZone(expiresAt));
    }

    /**
     * Called every few seconds by TempPasswordExpiryJob. For each user whose default password time is over:
     * if they still have not set their own password, the default password is replaced with a random secret,
     * so logging in with the default password no longer works. An ADMIN can reset it again.
     */
    public void expireUnusedPasswords() {
        for (Member member : memberRepository.findByTempPasswordExpiresAtBefore(LocalDateTime.now())) {
            try {
                if (keycloakAdminService.hasPendingPasswordChange(member.getKeycloakUserId())) {
                    keycloakAdminService.setPassword(member.getKeycloakUserId(),
                            UUID.randomUUID() + "Aa1!", true);
                    log.info("Default password expired for member {} ({})", member.getId(), member.getEmail());
                }
                member.setTempPasswordExpiresAt(null);
                memberRepository.save(member);
            } catch (RuntimeException e) {
                // Keycloak down etc. - keep the expiry time and try again on the next run
                log.warn("Could not expire default password for member {}: {}", member.getId(), e.getMessage());
            }
        }
    }

    public List<KeycloakAdminService.KeycloakUser> usersWithRole(String role) {
        return keycloakAdminService.usersWithRole(normalizeRole(role));
    }

    // e.g. 2026-11-20T10:49:38Z - the zone makes the time clear in any time zone
    private static OffsetDateTime withZone(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private static String normalizeRole(String role) {
        String value = required(role, "role").toUpperCase(Locale.ROOT);
        if (!CREATABLE_ROLES.contains(value)) {
            throw badRequest("role must be LIBRARIAN or MEMBER");
        }
        return value;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw badRequest(field + " is required");
        }
        return value.trim();
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}

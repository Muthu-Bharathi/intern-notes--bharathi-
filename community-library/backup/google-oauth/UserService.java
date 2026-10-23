package com.example.community_library.service;

import com.example.community_library.entity.AppUser;
import com.example.community_library.entity.Role;
import com.example.community_library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String GOOGLE = "google";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Indha emails-la Google login panna ADMIN role kedaikkum (ADMIN_EMAILS env variable, comma separated)
    @Value("${app.security.admin-emails:}")
    private Set<String> adminEmails;

    /**
     * Google login success aana apram call aagum.
     * Google "sub" (unique id) vachu user irukka nu paakrom; illa na pudhusa create panrom.
     */
    @Transactional
    public AppUser processGoogleUser(OAuth2User googleUser) {
        String providerId = googleUser.getAttribute("sub");
        String email = googleUser.getAttribute("email");
        String name = googleUser.getAttribute("name");

        AppUser user = userRepository.findByProviderAndProviderId(GOOGLE, providerId)
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setProvider(GOOGLE);
                    newUser.setProviderId(providerId);
                    newUser.setUsername(uniqueUsername(email, providerId));
                    // Google users never log in with a password; store an unguessable hash
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    return newUser;
                });

        user.setEmail(email);
        user.setName(name);
        // Admin list-la irundha ADMIN; illa na already irukkura role (new user-ku MEMBER) apdiye irukkum
        user.setRole(isConfiguredAdmin(googleUser) ? Role.ADMIN : user.getRole());
        return userRepository.save(user);
    }

    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    public AppUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public AppUser updateRole(Long id, Role role, Long currentUserId) {
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role is required");
        }
        // Admin thannoda role-ah maathina, system-la admin-eh illama poga chance irukku
        if (id.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change your own role");
        }
        AppUser user = getUserById(id);
        user.setRole(role);
        return userRepository.save(user);
    }

    private boolean isConfiguredAdmin(OAuth2User googleUser) {
        String email = googleUser.getAttribute("email");
        // Verify aagadha email-ah nambi ADMIN kudukka koodadhu
        if (email == null || !Boolean.TRUE.equals(googleUser.getAttribute("email_verified"))) {
            return false;
        }
        return adminEmails.stream().anyMatch(email::equalsIgnoreCase);
    }

    private String uniqueUsername(String email, String providerId) {
        String base = GOOGLE + "_" + (email != null ? email : providerId);
        return userRepository.existsByUsername(base) ? base + "_" + providerId : base;
    }
}

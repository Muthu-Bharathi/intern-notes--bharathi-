package com.example.community_library;

import com.example.community_library.entity.Member;
import com.example.community_library.repository.MemberRepository;
import com.example.community_library.service.UserAdminService;
import com.example.community_library.security.KeycloakRoleConverter;
import com.example.community_library.security.KeycloakAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Keycloak Admin API is mocked, so Keycloak need not be running.
@SpringBootTest
@AutoConfigureMockMvc
class AdminUserTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeycloakRoleConverter keycloakRoleConverter;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserAdminService userAdminService;

    @MockitoBean
    private KeycloakAdminService keycloakAdminService;

    @org.springframework.beans.factory.annotation.Value("${app.keycloak.default-user-password}")
    private String defaultUserPassword;

    @Test
    void onlyAdminCanCreateUsers() throws Exception {
        String body = userJson("x", "LIBRARIAN", "x@library.local");
        mockMvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/admin/users").with(token("librarian1", "LIBRARIAN"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/users?role=MEMBER").with(token("member1", "MEMBER")))
                .andExpect(status().isForbidden());
        verify(keycloakAdminService, never()).createUser(any(), any(), any(), any(), any(), anyBoolean(), any());
    }

    @Test
    void adminCreatesLibrarianInKeycloakAndLibraryDb() throws Exception {
        String libEmail = "lib2-" + UUID.randomUUID().toString().substring(0, 8) + "@library.local";
        when(keycloakAdminService.createUser(eq("lib2"), any(), any(), any(), any(), eq(true), eq("LIBRARIAN")))
                .thenReturn("kc-lib2");

        mockMvc.perform(post("/api/admin/users").with(token("admin1", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("lib2", "librarian", libEmail)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.keycloakUserId").value("kc-lib2"))
                .andExpect(jsonPath("$.role").value("LIBRARIAN"))
                .andExpect(jsonPath("$.temporaryPassword").value(defaultUserPassword))
                .andExpect(jsonPath("$.temporaryPasswordExpiresAt").exists())
                .andExpect(jsonPath("$.passwordChangeRequired").value(true))
                .andExpect(jsonPath("$.memberId").isNumber());

        var saved = memberRepository.findAll().stream()
                .filter(m -> "kc-lib2".equals(m.getKeycloakUserId())).findFirst().orElseThrow();
        memberRepository.delete(saved);
    }

    @Test
    void adminCreatesMemberInKeycloakAndLibraryDb() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "m-" + unique + "@library.local";
        String keycloakId = UUID.randomUUID().toString();
        when(keycloakAdminService.createUser(any(), eq(email), any(), any(), any(), eq(true), eq("MEMBER")))
                .thenReturn(keycloakId);

        mockMvc.perform(post("/api/admin/users").with(token("admin1", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("m" + unique, "MEMBER", email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.memberId").isNumber());

        var saved = memberRepository.findAll().stream()
                .filter(m -> keycloakId.equals(m.getKeycloakUserId())).findFirst().orElseThrow();
        memberRepository.delete(saved);
    }

    @Test
    void librarianAddingMemberAlwaysCreatesMemberRole() throws Exception {
        String email = "m-" + UUID.randomUUID().toString().substring(0, 8) + "@library.local";
        String keycloakId = UUID.randomUUID().toString();
        when(keycloakAdminService.createUser(eq(email), eq(email), any(), any(), any(), eq(true), eq("MEMBER")))
                .thenReturn(keycloakId);

        mockMvc.perform(post("/api/members").with(token("librarian1", "LIBRARIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ravi Kumar","email":"%s","password":"Secret#123","role":"ADMIN"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("MEMBER"));

        var saved = memberRepository.findAll().stream()
                .filter(m -> keycloakId.equals(m.getKeycloakUserId())).findFirst().orElseThrow();
        memberRepository.delete(saved);
    }

    @Test
    void swaggerPlaceholderPasswordIsIgnoredAndDefaultIsUsed() throws Exception {
        String email = "r-" + UUID.randomUUID().toString().substring(0, 8) + "@library.local";
        String keycloakId = UUID.randomUUID().toString();
        when(keycloakAdminService.createUser(eq("rakesh-" + email.substring(2, 10)), eq(email), any(), any(),
                eq(defaultUserPassword),
                eq(true), eq("MEMBER"))).thenReturn(keycloakId);

        mockMvc.perform(post("/api/admin/users").with(token("admin1", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"rakesh-%s","password":"string","email":"%s","firstName":"Rakesh",
                                 "lastName":"Kumar","role":"MEMBER","phone":"9000000002","age":27}
                                """.formatted(email.substring(2, 10), email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.temporaryPassword").value(defaultUserPassword))
                .andExpect(jsonPath("$.passwordChangeRequired").value(true));

        var saved = memberRepository.findAll().stream()
                .filter(m -> keycloakId.equals(m.getKeycloakUserId())).findFirst().orElseThrow();
        memberRepository.delete(saved);
    }

    @Test
    void unchangedDefaultPasswordExpiresAfterValidityTime() throws Exception {
        Member member = savedMemberWithExpiry(LocalDateTime.now().minusSeconds(5));
        when(keycloakAdminService.hasPendingPasswordChange(member.getKeycloakUserId())).thenReturn(true);

        userAdminService.expireUnusedPasswords();

        // default password replaced by a random secret, still temporary
        verify(keycloakAdminService).setPassword(eq(member.getKeycloakUserId()),
                argThat(pw -> !pw.equals(defaultUserPassword)), eq(true));
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getTempPasswordExpiresAt()).isNull();
        memberRepository.delete(member);
    }

    @Test
    void changedPasswordIsNotTouchedByExpiry() throws Exception {
        Member member = savedMemberWithExpiry(LocalDateTime.now().minusSeconds(5));
        when(keycloakAdminService.hasPendingPasswordChange(member.getKeycloakUserId())).thenReturn(false);

        userAdminService.expireUnusedPasswords();

        verify(keycloakAdminService, never()).setPassword(eq(member.getKeycloakUserId()), any(), anyBoolean());
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getTempPasswordExpiresAt()).isNull();
        memberRepository.delete(member);
    }

    @Test
    void passwordStillValidIsNotExpiredYet() throws Exception {
        Member member = savedMemberWithExpiry(LocalDateTime.now().plusSeconds(100));

        userAdminService.expireUnusedPasswords();

        verify(keycloakAdminService, never()).setPassword(eq(member.getKeycloakUserId()), any(), anyBoolean());
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getTempPasswordExpiresAt()).isNotNull();
        memberRepository.delete(member);
    }

    @Test
    void adminCanGiveTheDefaultPasswordAgain() throws Exception {
        Member member = savedMemberWithExpiry(null);

        mockMvc.perform(post("/api/admin/users/" + member.getId() + "/reset-password").with(token("admin1", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temporaryPassword").value(defaultUserPassword))
                .andExpect(jsonPath("$.temporaryPasswordExpiresAt").exists());
        mockMvc.perform(post("/api/admin/users/" + member.getId() + "/reset-password").with(token("lib", "LIBRARIAN")))
                .andExpect(status().isForbidden());

        verify(keycloakAdminService).setPassword(member.getKeycloakUserId(), defaultUserPassword, true);
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getTempPasswordExpiresAt()).isNotNull();
        memberRepository.delete(member);
    }

    private Member savedMemberWithExpiry(LocalDateTime expiresAt) {
        String unique = UUID.randomUUID().toString();
        Member member = new Member();
        member.setName("Expiry Test");
        member.setEmail("exp-" + unique.substring(0, 8) + "@library.local");
        member.setRole("MEMBER");
        member.setKeycloakUserId(unique);
        member.setTempPasswordExpiresAt(expiresAt);
        return memberRepository.save(member);
    }

    @Test
    void adminCannotCreateAnotherAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/users").with(token("admin1", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("boss", "ADMIN", "boss@library.local")))
                .andExpect(status().isBadRequest());
    }

    private static String userJson(String username, String role, String email) {
        return """
                {"username":"%s","password":"Secret#123","email":"%s","firstName":"Test","lastName":"User",
                 "role":"%s","phone":"9999999999","age":30}
                """.formatted(username, email, role);
    }

    private RequestPostProcessor token(String username, String... realmRoles) {
        return jwt()
                .jwt(jwt -> jwt.subject("user-id-" + username)
                        .claim("preferred_username", username)
                        .claim("realm_access", Map.of("roles", List.of(realmRoles))))
                .authorities(keycloakRoleConverter);
    }
}

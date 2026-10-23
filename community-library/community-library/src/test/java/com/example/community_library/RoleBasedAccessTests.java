package com.example.community_library;

import com.example.community_library.security.KeycloakRoleConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tokens here have the same claims Keycloak puts in a real access token, so Keycloak need not be running.
// Write requests send a broken JSON body on purpose: if the role is allowed we get 400 (nothing is saved),
// if the role is not allowed security stops it earlier with 401/403.
@SpringBootTest
@AutoConfigureMockMvc
class RoleBasedAccessTests {

    private static final String BROKEN_JSON = "{";
    private static final String UNKNOWN_ID = "/999999999";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeycloakRoleConverter keycloakRoleConverter;

    @Test
    void swaggerPagesOpenWithoutLogin() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void everyApiRejectsRequestsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/books")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/books" + UNKNOWN_ID)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/members")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/loans")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(write(post("/api/books"))).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/books" + UNKNOWN_ID)).andExpect(status().isUnauthorized());
    }

    @Test
    void malformedTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/loans").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userWithoutLibraryRoleCanOnlySeeOwnDetails() throws Exception {
        RequestPostProcessor token = keycloakToken("newuser", "default-roles-community-library");

        mockMvc.perform(get("/api/auth/me").with(token)).andExpect(status().isOk());
        mockMvc.perform(get("/api/books").with(token)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/loans").with(token)).andExpect(status().isForbidden());
    }

    @Test
    void memberCanOnlyReadLoans() throws Exception {
        RequestPostProcessor token = keycloakToken("member1", "MEMBER");

        mockMvc.perform(get("/api/books").with(token)).andExpect(status().isOk());
        mockMvc.perform(get("/api/loans").with(token)).andExpect(status().isOk());
        mockMvc.perform(write(post("/api/loans")).with(token)).andExpect(status().isForbidden());
        mockMvc.perform(write(post("/api/books")).with(token)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/members").with(token)).andExpect(status().isForbidden());
    }

    @Test
    void librarianManagesBooksMembersAndLoansButCannotDelete() throws Exception {
        RequestPostProcessor token = keycloakToken("librarian1", "LIBRARIAN");

        mockMvc.perform(get("/api/members").with(token)).andExpect(status().isOk());
        mockMvc.perform(get("/api/loans").with(token)).andExpect(status().isOk());
        mockMvc.perform(write(post("/api/books")).with(token)).andExpect(status().isBadRequest());
        mockMvc.perform(write(post("/api/members")).with(token)).andExpect(status().isBadRequest());
        mockMvc.perform(write(post("/api/loans")).with(token)).andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/books" + UNKNOWN_ID).with(token)).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/members" + UNKNOWN_ID).with(token)).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/loans" + UNKNOWN_ID).with(token)).andExpect(status().isForbidden());
    }

    @Test
    void adminInheritsLibrarianAndMemberAccess() throws Exception {
        RequestPostProcessor token = keycloakToken("admin1", "ADMIN");

        mockMvc.perform(get("/api/members").with(token)).andExpect(status().isOk());
        mockMvc.perform(get("/api/loans").with(token)).andExpect(status().isOk());
        mockMvc.perform(write(post("/api/books")).with(token)).andExpect(status().isBadRequest());
    }

    @Test
    void clientRolesInResourceAccessAlsoCount() throws Exception {
        RequestPostProcessor token = jwt()
                .jwt(jwt -> jwt.claim("resource_access",
                        Map.of("community-library-app", Map.of("roles", List.of("LIBRARIAN")))))
                .authorities(keycloakRoleConverter);

        mockMvc.perform(get("/api/members").with(token)).andExpect(status().isOk());
    }

    @Test
    void meReturnsUserDetailsAndRolesFromTheToken() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(keycloakToken("librarian1", "LIBRARIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-id-librarian1"))
                .andExpect(jsonPath("$.username").value("librarian1"))
                .andExpect(jsonPath("$.email").value("librarian1@library.local"))
                .andExpect(jsonPath("$.name").value("Test librarian1"))
                .andExpect(jsonPath("$.roles[0]").value("LIBRARIAN"));
    }

    // Same shape as a Keycloak access token: realm roles under realm_access.roles
    private RequestPostProcessor keycloakToken(String username, String... realmRoles) {
        return jwt()
                .jwt(jwt -> jwt
                        .subject("user-id-" + username)
                        .claim("preferred_username", username)
                        .claim("email", username + "@library.local")
                        .claim("name", "Test " + username)
                        .claim("realm_access", Map.of("roles", List.of(realmRoles))))
                .authorities(keycloakRoleConverter);
    }

    private MockHttpServletRequestBuilder write(MockHttpServletRequestBuilder request) {
        return request.contentType(MediaType.APPLICATION_JSON).content(BROKEN_JSON);
    }
}

package com.example.community_library.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Talks to the Keycloak Admin REST API so LIBRARIAN / MEMBER logins can be created from the app.
 * The users and their roles live in Keycloak - the app still reads roles only from the JWT.
 *
 * The app logs in with the Keycloak admin account from .env (KEYCLOAK_ADMIN / KEYCLOAK_ADMIN_PASSWORD)
 * through the built-in "admin-cli" client of the master realm, then calls /admin/realms/{realm}/...
 * Any failure is reported back to the caller (no silent skip), so the app never says
 * "created" when the Keycloak login was not made.
 */
@Service
public class KeycloakAdminService {

    private static final String ADMIN_AUTH_REALM = "master";
    private static final String ADMIN_CLIENT_ID = "admin-cli";

    private final RestClient restClient;
    private final String realm;
    private final String adminUsername;
    private final String adminPassword;

    public KeycloakAdminService(
            @Value("${app.keycloak.server-url:http://localhost:8180}") String serverUrl,
            @Value("${app.keycloak.realm:community-library}") String realm,
            @Value("${app.keycloak.admin-username:admin}") String adminUsername,
            @Value("${app.keycloak.admin-password:}") String adminPassword) {
        this.restClient = RestClient.builder().baseUrl(serverUrl.replaceAll("/$", "")).build();
        this.realm = realm;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    /** A user as Keycloak returns it (only the fields we use). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KeycloakUser(String id, String username, String email, String firstName, String lastName,
                               Boolean enabled) {
    }

    /**
     * Creates the user in the realm with a password and gives it one realm role.
     * temporaryPassword = true: Keycloak makes the user set a new password at the first login.
     * Returns the new Keycloak user id. If the role cannot be assigned, the user is removed again.
     */
    public String createUser(String username, String email, String firstName, String lastName,
                             String password, boolean temporaryPassword, String role) {
        String token = adminToken();

        Map<String, Object> user = Map.of(
                "username", username,
                "email", email,
                "firstName", firstName,
                "lastName", lastName,
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(Map.of("type", "password", "value", password, "temporary", temporaryPassword)));

        ResponseEntity<Void> created;
        try {
            created = restClient.post()
                    .uri("/admin/realms/{realm}/users", realm)
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A Keycloak user with this username or email already exists");
            }
            if (e.getStatusCode().value() == HttpStatus.BAD_REQUEST.value()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Keycloak rejected the user: " + e.getResponseBodyAsString());
            }
            throw keycloakError("create the user", e);
        } catch (RestClientException e) {
            throw keycloakDown();
        }

        String userId = idFromLocation(created.getHeaders().getLocation());

        try {
            assignRealmRole(token, userId, role);
        } catch (RuntimeException e) {
            deleteUserQuietly(token, userId);
            throw e;
        }
        return userId;
    }

    /** Users that have the given realm role, e.g. all LIBRARIAN users. */
    public List<KeycloakUser> usersWithRole(String role) {
        String token = adminToken();
        try {
            KeycloakUser[] users = restClient.get()
                    .uri("/admin/realms/{realm}/roles/{role}/users?max=1000", realm, role)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .body(KeycloakUser[].class);
            return users == null ? List.of() : List.of(users);
        } catch (RestClientResponseException e) {
            throw keycloakError("list users with role " + role, e);
        } catch (RestClientException e) {
            throw keycloakDown();
        }
    }

    /** Sets a new password. temporary = true: the user must choose their own password at the next login. */
    public void setPassword(String userId, String password, boolean temporary) {
        String token = adminToken();
        try {
            restClient.put()
                    .uri("/admin/realms/{realm}/users/{id}/reset-password", realm, userId)
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("type", "password", "value", password, "temporary", temporary))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw keycloakError("set the password", e);
        } catch (RestClientException e) {
            throw keycloakDown();
        }
    }

    /** True while the user still has to replace their temporary password (UPDATE_PASSWORD required action). */
    public boolean hasPendingPasswordChange(String userId) {
        String token = adminToken();
        try {
            Map<?, ?> user = restClient.get()
                    .uri("/admin/realms/{realm}/users/{id}", realm, userId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .body(Map.class);
            return user != null && user.get("requiredActions") instanceof List<?> actions
                    && actions.contains("UPDATE_PASSWORD");
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return false; // user was deleted in Keycloak
            }
            throw keycloakError("read the user", e);
        } catch (RestClientException e) {
            throw keycloakDown();
        }
    }

    /** Removes a Keycloak user (used to undo a half-finished create). */
    public void deleteUser(String userId) {
        deleteUserQuietly(adminToken(), userId);
    }

    private void assignRealmRole(String token, String userId, String role) {
        try {
            Map<?, ?> roleRepresentation = restClient.get()
                    .uri("/admin/realms/{realm}/roles/{role}", realm, role)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .body(Map.class);

            restClient.post()
                    .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", realm, userId)
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(roleRepresentation))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw keycloakError("assign role " + role, e);
        } catch (RestClientException e) {
            throw keycloakDown();
        }
    }

    private void deleteUserQuietly(String token, String userId) {
        try {
            restClient.delete()
                    .uri("/admin/realms/{realm}/users/{id}", realm, userId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ignored) {
            // best effort clean-up
        }
    }

    private String adminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", ADMIN_CLIENT_ID);
        form.add("username", adminUsername);
        form.add("password", adminPassword);
        try {
            Map<?, ?> response = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", ADMIN_AUTH_REALM)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
            Object token = response == null ? null : response.get("access_token");
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak did not return an admin token");
            }
            return token.toString();
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not log in to the Keycloak admin API - check KEYCLOAK_ADMIN / KEYCLOAK_ADMIN_PASSWORD in .env");
        } catch (RestClientException e) {
            throw keycloakDown();
        }
    }

    private static String idFromLocation(URI location) {
        if (location == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak did not return the new user's id");
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static ResponseStatusException keycloakError(String action, RestClientResponseException e) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Keycloak could not " + action + " (HTTP " + e.getStatusCode().value() + ")");
    }

    private static ResponseStatusException keycloakDown() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Keycloak is not reachable - is it running on port 8180?");
    }
}

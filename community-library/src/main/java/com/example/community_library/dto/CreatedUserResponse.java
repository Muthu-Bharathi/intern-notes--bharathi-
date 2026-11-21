package com.example.community_library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

// Result of POST /api/admin/users
@Getter
@AllArgsConstructor
public class CreatedUserResponse {

    private Long memberId;          // row in the members table
    private String keycloakUserId;  // id of the user in 
    private String username;        // Keycloak login name
    private String email;
    private String role;
    private String temporaryPassword;          // default first-login password to give to the user
    private boolean passwordChangeRequired;    // always true: Keycloak asks for a new password at first login
    private OffsetDateTime temporaryPasswordExpiresAt;  // after this the default password no longer works
}

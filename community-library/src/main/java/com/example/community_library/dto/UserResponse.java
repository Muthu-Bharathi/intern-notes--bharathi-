package com.example.community_library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

// Logged-in user details - ellam Keycloak JWT claims-la irundhu varudhu, DB-la illa
@Getter
@AllArgsConstructor
public class UserResponse {

    private String id;        // "sub" claim - Keycloak user id
    private String username;  // "preferred_username"
    private String email;
    private String name;
    private Set<String> roles;
}

package com.example.community_library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private Integer age;

    @Column(nullable = false)
    private String role;

    // Keycloak user id of this person's login (null for rows created before logins were linked)
    @Column(name = "keycloak_user_id", unique = true, length = 36)
    private String keycloakUserId;

    // When the default first-login password stops working (null = nothing pending)
    @Column(name = "temp_password_expires_at")
    private java.time.LocalDateTime tempPasswordExpiresAt;
}

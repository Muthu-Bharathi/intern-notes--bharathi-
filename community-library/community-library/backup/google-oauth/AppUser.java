package com.example.community_library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "app_users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Random BCrypt hash - Google users never log in with a password

    private String email;

    private String name;

    // OAuth provider, e.g. "google"
    private String provider;

    // Provider's unique user id (Google "sub")
    @Column(name = "provider_id")
    private String providerId;

    // Nullable: role column add aagura munnadi irundha old rows-ku null irukkum - adha MEMBER-ah treat panrom
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private Role role;

    public Role getRole() {
        return role != null ? role : Role.MEMBER;
    }

}

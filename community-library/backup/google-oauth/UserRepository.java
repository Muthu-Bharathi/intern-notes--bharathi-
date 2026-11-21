package com.example.community_library.repository;

import com.example.community_library.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<AppUser> findByProviderAndProviderId(String provider, String providerId);
}

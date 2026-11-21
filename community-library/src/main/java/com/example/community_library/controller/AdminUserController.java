package com.example.community_library.controller;

import com.example.community_library.dto.CreateUserRequest;
import com.example.community_library.dto.CreatedUserResponse;
import com.example.community_library.entity.Member;
import com.example.community_library.security.KeycloakAdminService;
import com.example.community_library.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// ADMIN only (see SecurityConfig): create LIBRARIAN / MEMBER logins in Keycloak + the library DB
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserAdminService userAdminService;

    
    @PostMapping
    public ResponseEntity<CreatedUserResponse> createUser(@RequestBody CreateUserRequest request) {
        UserAdminService.CreatedUser created = userAdminService.createUser(request);
        Member member = created.member();
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreatedUserResponse(
                member.getId(), member.getKeycloakUserId(), created.username(), member.getEmail(), member.getRole(),
                created.temporaryPassword(), true, created.expiresAt()));
    }

    // POST give the default password again, e.g. when it expired before the first login - 200 OK
    @PostMapping("/{memberId}/reset-password")
    public ResponseEntity<CreatedUserResponse> resetPassword(@PathVariable Long memberId) {
        UserAdminService.CreatedUser reset = userAdminService.resetToDefaultPassword(memberId);
        Member member = reset.member();
        return ResponseEntity.ok(new CreatedUserResponse(
                member.getId(), member.getKeycloakUserId(), null, member.getEmail(), member.getRole(),
                reset.temporaryPassword(), true, reset.expiresAt()));
    }

    
    @GetMapping
    public ResponseEntity<List<KeycloakAdminService.KeycloakUser>> listUsers(@RequestParam String role) {
        return ResponseEntity.ok(userAdminService.usersWithRole(role));
    }
}

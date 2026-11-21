package com.example.community_library.controller;

import com.example.community_library.dto.MemberRequest;
import com.example.community_library.dto.MemberResponse;
import com.example.community_library.entity.Member;
import com.example.community_library.service.MemberService;
import com.example.community_library.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // GET all members - 200 OK
    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        List<MemberResponse> response = memberService.getAllMembers()
                .stream()
                .map(MemberResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    // GET member by ID - 200 OK
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return ResponseEntity.ok(MemberResponse.fromEntity(member));
    }

    // POST create member (also creates the Keycloak login with role MEMBER) - 201 Created
    // Librarians are created by an ADMIN via POST /api/admin/users
    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberRequest request) {
        UserAdminService.CreatedUser created = memberService.createMember(request);
        MemberResponse response = MemberResponse.fromEntity(created.member());
        response.setTemporaryPassword(created.temporaryPassword());
        response.setTemporaryPasswordExpiresAt(created.expiresAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT update member - 200 OK
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberRequest request) {
        Member updated = memberService.updateMember(id, request);
        return ResponseEntity.ok(MemberResponse.fromEntity(updated));
    }

    // DELETE member - 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}

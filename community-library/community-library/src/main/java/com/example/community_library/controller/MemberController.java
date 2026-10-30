package com.example.community_library.controller;

import com.example.community_library.entity.Member;
import com.example.community_library.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // GET all members
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @GetMapping
    public List<Member> getAllMembers() {

        return memberService.getAllMembers();
    }

    // GET member by ID
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @GetMapping("/{id}")
    public Member getMemberById(@PathVariable Long id) {
        return memberService.getMemberById(id);
    }

    // POST create member
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        Member created = memberService.createMember(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT update member
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @PutMapping("/{id}")
    public Member updateMember(
            @PathVariable Long id,
            @RequestBody Member member) {

        return memberService.updateMember(id, member);
    }

    // DELETE member
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return "Member deleted successfully";
    }
}
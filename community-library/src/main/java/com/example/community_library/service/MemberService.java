package com.example.community_library.service;

import com.example.community_library.dto.CreateUserRequest;
import com.example.community_library.dto.MemberRequest;
import com.example.community_library.entity.Member;
import com.example.community_library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserAdminService userAdminService;

    // LIBRARIAN / ADMIN adds a member: Keycloak login (role MEMBER, username = email) + members row.
    // The role in the request is ignored here, so a librarian cannot create librarians or admins.
    public UserAdminService.CreatedUser createMember(MemberRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        String[] parts = name.split("\\s+", 2);

        CreateUserRequest user = new CreateUserRequest();
        user.setEmail(request.getEmail());
        user.setFirstName(parts[0]);
        user.setLastName(parts.length > 1 ? parts[1] : "");
        user.setPhone(request.getPhone());
        user.setAge(request.getAge());
        user.setRole("MEMBER");
        return userAdminService.createUser(user);
    }

    public Member createMember(Member request) {
        Member member = new Member();
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setAge(request.getAge());
        member.setRole(request.getRole() != null ? request.getRole() : "MEMBER");

        return memberRepository.save(member);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    public Member updateMember(Long id, MemberRequest request) {
        Member member = getMemberById(id);
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setAge(request.getAge());
        // role is not changed here: it lives in Keycloak (change it in the Keycloak console)

        return memberRepository.save(member);
    }

    public Member updateMember(Long id, Member request) {
        Member member = getMemberById(id);
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setAge(request.getAge());

        return memberRepository.save(member);
    }

    public void deleteMember(Long id) {
        Member member = getMemberById(id);
        memberRepository.delete(member);
    }

    public static String normalizeRole(String rawRole) {
        if (rawRole != null && !rawRole.isBlank()) {
            String upper = rawRole.trim().toUpperCase();
            if ("ADMIN".equals(upper) || "LIBRARIAN".equals(upper) || "MEMBER".equals(upper)) {
                return upper;
            }
        }
        return "MEMBER";
    }
}

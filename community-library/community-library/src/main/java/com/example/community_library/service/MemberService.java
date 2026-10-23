package com.example.community_library.service;

import com.example.community_library.entity.Member;
import com.example.community_library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member createMember(Member request) {

        Member member = new Member();

        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());

        return memberRepository.save(member);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    public Member updateMember(Long id, Member request) {

        Member member = getMemberById(id);

        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());

        return memberRepository.save(member);
    }

    public void deleteMember(Long id) {

        Member member = getMemberById(id);

        memberRepository.delete(member);
    }
}
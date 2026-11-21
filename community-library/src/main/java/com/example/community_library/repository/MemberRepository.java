package com.example.community_library.repository;

import com.example.community_library.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmailIgnoreCase(String email);

    List<Member> findByTempPasswordExpiresAtBefore(LocalDateTime time);
}

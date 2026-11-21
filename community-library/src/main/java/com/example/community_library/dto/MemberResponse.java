package com.example.community_library.dto;

import com.example.community_library.entity.Member;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Integer age;
    private String role;

    // Only in the response of POST /api/members: first-login password (must be changed at first login)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String temporaryPassword;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private java.time.OffsetDateTime temporaryPasswordExpiresAt;

    public static MemberResponse fromEntity(Member member) {
        if (member == null) {
            return null;
        }
        return new MemberResponse(member.getId(), member.getName(), member.getEmail(),
                member.getPhone(), member.getAge(), member.getRole(), null, null);
    }

    public static MemberResponse fromEntity(Member member, String role) {
        MemberResponse response = fromEntity(member);
        if (response != null && role != null && !role.isBlank()) {
            response.setRole(role.toUpperCase());
        }
        return response;
    }
}

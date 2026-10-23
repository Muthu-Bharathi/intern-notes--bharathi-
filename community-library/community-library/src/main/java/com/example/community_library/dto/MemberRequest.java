package com.example.community_library.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRequest {

    private String name;
    private String email;
    private String phone;
}
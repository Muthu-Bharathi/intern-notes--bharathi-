package com.example.community_library.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanRequest {

    private Long bookId;
    private Long memberId;
}
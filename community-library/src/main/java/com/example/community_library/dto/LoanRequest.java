package com.example.community_library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {

    private Long bookId;
    private Long memberId;
    private IdHolder book;
    private IdHolder member;

    public Long getResolvedBookId() {
        if (bookId != null) {
            return bookId;
        }
        if (book != null) {
            return book.getId();
        }
        return null;
    }

    public Long getResolvedMemberId() {
        if (memberId != null) {
            return memberId;
        }
        if (member != null) {
            return member.getId();
        }
        return null;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdHolder {
        private Long id;
    }
}

package com.example.community_library.dto;

import com.example.community_library.entity.Loan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private Long id;
    private BookResponse book;
    private MemberResponse member;
    private LocalDate loanDate;
    private LocalDate returnDate;
    private String status;

    public static LoanResponse fromEntity(Loan loan) {
        if (loan == null) {
            return null;
        }
        return new LoanResponse(
                loan.getId(),
                BookResponse.fromEntity(loan.getBook()),
                MemberResponse.fromEntity(loan.getMember()),
                loan.getLoanDate(),
                loan.getReturnDate(),
                loan.getStatus()
        );
    }
}

package com.example.community_library.service;

import com.example.community_library.entity.Book;
import com.example.community_library.entity.Loan;
import com.example.community_library.entity.Member;
import com.example.community_library.repository.BookRepository;
import com.example.community_library.repository.LoanRepository;
import com.example.community_library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public Loan createLoan(Loan request) {

        Book book = bookRepository.findById(request.getBook().getId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Member member = memberRepository.findById(request.getMember().getId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!book.getAvailable()) {
            throw new RuntimeException("Book is not available");
        }

        Loan loan = new Loan();

        loan.setBook(book);
        loan.setMember(member);
        loan.setLoanDate(LocalDate.now());
        loan.setStatus("BORROWED");

        book.setAvailable(false);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
    }

    public Loan updateLoan(Long id, Loan request) {

        Loan loan = getLoanById(id);

        Book book = bookRepository.findById(request.getBook().getId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Member member = memberRepository.findById(request.getMember().getId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        loan.setBook(book);
        loan.setMember(member);

        return loanRepository.save(loan);
    }

    public void deleteLoan(Long id) {

        Loan loan = getLoanById(id);

        if ("BORROWED".equals(loan.getStatus())) {
            Book book = loan.getBook();
            book.setAvailable(true);
            bookRepository.save(book);
        }

        loanRepository.delete(loan);
    }

    public Loan returnLoan(Long id) {

        Loan loan = getLoanById(id);

        if ("RETURNED".equals(loan.getStatus())) {
            throw new RuntimeException("Book is already returned");
        }

        loan.setStatus("RETURNED");
        loan.setReturnDate(LocalDate.now());

        Book book = loan.getBook();
        book.setAvailable(true);

        bookRepository.save(book);

        return loanRepository.save(loan);
    }
}
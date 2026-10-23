package com.example.community_library.service;


import com.example.community_library.entity.Book;
import com.example.community_library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


//private final BookRepository bookRepository;
//
//public BookService(BookRepository bookRepository) {
//   this.bookRepository = bookRepository;
//}

@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;

    public Book createBook(Book request) {

        Book book = new Book();

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setAvailable(true);

        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public Book updateBook(Long id, Book request) {

        Book book = getBookById(id);

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());

        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
    }
}
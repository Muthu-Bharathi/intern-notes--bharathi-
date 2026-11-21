package com.example.community_library.controller;

import com.example.community_library.dto.BookRequest;
import com.example.community_library.dto.BookResponse;
import com.example.community_library.entity.Book;
import com.example.community_library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor 
public class BookController {

    private final BookService bookService;

    // GET all books - 200 OK
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> response = bookService.getAllBooks()
                .stream()
                .map(BookResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    // GET book by ID - 200 OK
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(BookResponse.fromEntity(book));
    }

    // POST create book - 201 Created
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        Book created = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.fromEntity(created));
    }

    // PUT update book - 200 OK
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        Book updated = bookService.updateBook(id, request);
        return ResponseEntity.ok(BookResponse.fromEntity(updated));
    }

    // DELETE book - 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}

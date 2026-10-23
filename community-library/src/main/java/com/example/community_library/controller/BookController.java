package com.example.community_library.controller;

import com.example.community_library.entity.Book;
import com.example.community_library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.status(200).body(bookService.getAllBooks());
    }

    // GET book by ID - 200 OK
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(bookService.getBookById(id));
    }

    // POST create book - 201 Created
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        Book created = bookService.createBook(book);
        return ResponseEntity.status(201).body(created);
    }

    // PUT update book - 200 OK
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody Book book) {
        Book updated = bookService.updateBook(id, book);
        return ResponseEntity.status(200).body(updated);
    }

    // DELETE book - 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.status(204).build();
    }
}
package com.example.community_library.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookRequest {

    private String title;
    private String author;
    private String isbn;
}
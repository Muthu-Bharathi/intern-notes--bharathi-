package com.example.community_library.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreateUserRequest {

    private String username;   
    private String email;
    private String firstName;
    private String lastName;
    private String role;       

    private String phone;
    private Integer age;
}

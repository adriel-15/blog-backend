package com.arprojects.blog.domain.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record SignUpDto(

        @NotBlank (message = "Username is required")
        @Size(min = 10,message = "username must be at least 10 characters long")
        String username,
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid (e.g., user@example.com")
        String email,
        @NotBlank(message = "Profile Name required")
        @Pattern(
                regexp = "^[a-zA-Z][a-zA-Z ]*$",  // Allows spaces after the first letter
                message = "Must start with a letter and only contain letters/spaces"
        )
        String profileName,

        @Past
        LocalDate birthDate,

        @NotBlank
        @Size(min = 10, message = "password must be at least 10 characters long")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
                message = "Password must contain at least one uppercase letter and one special symbol (@, #, $, etc.)"
        )
        String password

) {
}

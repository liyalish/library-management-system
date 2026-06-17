package com.library.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "{validation.username.required}")
    @Size(min = 3, max = 50, message = "{validation.username.size}")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "{validation.username.pattern}")
    private String username;

    @NotBlank(message = "{validation.fullName.required}")
    @Size(max = 100, message = "{validation.fullName.size}")
    private String fullName;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 4, max = 100, message = "{validation.password.size}")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
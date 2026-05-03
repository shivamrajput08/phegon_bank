package com.phegon.phegonbank.auth_users.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;

@Data
public class RegistrationRequest {

    @NotBlank(message = "FirstName is required")
    private String firstName;

    private String lastName;
    private String phoneNumber;
    @NotBlank(message = "Email is required")
    private String email;
    private List<String> roles;

    @NotBlank(message = "Password is required")
    private String password;
}

package com.phegon.phegonbank.auth_users.dtos;

import lombok.Builder;
import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;

@Data
@Builder
public class LoginResponse {
    private String token;
    private List<String> roles;
}

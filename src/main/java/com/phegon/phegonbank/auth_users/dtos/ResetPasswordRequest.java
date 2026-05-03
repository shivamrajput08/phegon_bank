package com.phegon.phegonbank.auth_users.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResetPasswordRequest {
    private String email;
    private String code;
    private String newPassword;
}

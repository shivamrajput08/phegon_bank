package com.phegon.phegonbank.auth_users.repo;

import com.phegon.phegonbank.auth_users.entity.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.Optional;

public interface PasswordResetCodeRepo extends JpaRepository<PasswordResetCode , Long> {
    Optional<PasswordResetCode> findByCode(String code);
    void deleteByUserId(Long userId);
}

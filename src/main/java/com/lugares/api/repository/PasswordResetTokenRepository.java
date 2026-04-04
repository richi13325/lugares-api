package com.lugares.api.repository;

import com.lugares.api.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserEmail(String userEmail);

    Optional<PasswordResetToken> findByUserEmailAndToken(String userEmail, String token);

    void deleteByUserEmail(String userEmail);
}

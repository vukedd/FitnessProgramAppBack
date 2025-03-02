package com.app.fitness.fitnesprogramapp.repositories.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.fitness.fitnesprogramapp.models.auth.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByIdAndExpiresAtAfter(Long id, Instant date);
}

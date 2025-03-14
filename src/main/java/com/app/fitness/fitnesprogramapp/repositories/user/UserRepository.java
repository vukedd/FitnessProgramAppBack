package com.app.fitness.fitnesprogramapp.repositories.user;

import com.app.fitness.fitnesprogramapp.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);

    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.enabled = true")
    Optional<User> findVerifiedUserByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN RefreshToken rt ON u.id = rt.user.id WHERE rt.id = ?1")
    Optional<User> findUserByRefreshTokenId(Long refreshTokenId);
}

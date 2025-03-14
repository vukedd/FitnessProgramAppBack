package com.app.fitness.fitnesprogramapp.services.auth;

import com.app.fitness.fitnesprogramapp.dtos.auth.AuthenticationRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.auth.AuthenticationResponseDto;
import com.app.fitness.fitnesprogramapp.exceptions.customExceptions.LoginException;
import com.app.fitness.fitnesprogramapp.exceptions.customExceptions.ValidationException;
import com.app.fitness.fitnesprogramapp.repositories.auth.RefreshTokenRepository;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import com.app.fitness.fitnesprogramapp.models.auth.RefreshToken;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${application.security.jwt.refresh-token-ttl}")
    private Long refreshTokenExpireTime;

    public AuthenticationResponseDto authenticate(
            final AuthenticationRequestDto request) {


        final var authToken = UsernamePasswordAuthenticationToken
                .unauthenticated(request.getUsername(), request.getPassword());

        final var authentication = authenticationManager.authenticate(authToken);

        final var user = (User) authentication.getPrincipal();
        com.app.fitness.fitnesprogramapp.models.user.User userEntity = userRepository.findVerifiedUserByUsername(user.getUsername())
                .orElseThrow(() -> new LoginException("User account is not verified!", LoginException.ErrorType.NOT_VERIFIED));

        final var token = jwtService.generateToken(request.getUsername());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userEntity);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plus(Duration.ofMillis(refreshTokenExpireTime)));
        refreshTokenRepository.save(refreshToken);

        return new AuthenticationResponseDto(token, refreshToken.getId());
    }

    public AuthenticationResponseDto refreshToken(Long refreshTokenId) {
        final var refreshTokenEntity = refreshTokenRepository
                .findByIdAndExpiresAtAfter(refreshTokenId, Instant.now())
                .orElseThrow(() -> new ValidationException("Invalid or expired refresh token!", ValidationException.ErrorType.BAD_REQUEST));

        final var newAccessToken = jwtService.generateToken(refreshTokenEntity.getUser().getUsername());
        return new AuthenticationResponseDto(newAccessToken, refreshTokenId);
    }

    public void revokeRefreshToken(Long refreshTokenId) {
        refreshTokenRepository.deleteById(refreshTokenId);
    }
}

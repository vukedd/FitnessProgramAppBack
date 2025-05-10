package com.app.fitness.fitnesprogramapp.controllers.auth;

import com.app.fitness.fitnesprogramapp.dtos.auth.AuthenticationRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.auth.AuthenticationResponseDto;
import com.app.fitness.fitnesprogramapp.dtos.auth.RefreshTokenRequest;
import com.app.fitness.fitnesprogramapp.services.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> authenticate(
            @RequestBody AuthenticationRequestDto authenticationRequestDto) {
        return ResponseEntity.ok(authService.authenticate(authenticationRequestDto));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest.getRefreshTokenId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> revokeToken(@RequestParam Long refreshTokenId) {
        authService.revokeRefreshToken(refreshTokenId);
        return ResponseEntity.noContent().build();
    }
}

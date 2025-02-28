package com.app.fitness.fitnesprogramapp.controllers.auth;

import com.app.fitness.fitnesprogramapp.dtos.auth.AuthenticationRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.auth.AuthenticationResponseDto;
import com.app.fitness.fitnesprogramapp.services.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> authenticate(
            @RequestBody AuthenticationRequestDto authenticationRequestDto) {
        return ResponseEntity.ok(authService.authenticate(authenticationRequestDto));
    }
}

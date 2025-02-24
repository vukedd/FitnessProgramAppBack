package com.app.fitness.fitnesprogramapp.controllers;

import com.app.fitness.fitnesprogramapp.dtos.auth.UserRegisterRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.auth.UserRegisterResponseDto;
import com.app.fitness.fitnesprogramapp.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDto> registerUser(@RequestBody UserRegisterRequestDto request) {
        return ResponseEntity.ok(authService.sendUserRegisterRequest(request));
    }
}

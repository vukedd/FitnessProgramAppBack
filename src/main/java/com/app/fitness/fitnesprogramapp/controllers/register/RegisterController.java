package com.app.fitness.fitnesprogramapp.controllers.register;

import com.app.fitness.fitnesprogramapp.dtos.register.UserRegisterRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.register.UserRegisterResponseDto;
import com.app.fitness.fitnesprogramapp.dtos.verification.UserVerificationResponseDto;
import com.app.fitness.fitnesprogramapp.services.register.RegisterService;
import com.app.fitness.fitnesprogramapp.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.webauthn.api.UserVerificationRequirement;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class RegisterController {
    private final RegisterService userRegistrationService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDto> registerUser(@RequestBody UserRegisterRequestDto registrationRequest) {
        return ResponseEntity.ok(userRegistrationService.sendUserRegisterRequest(registrationRequest));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<UserVerificationResponseDto> verifyEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(userRegistrationService.validateVerificationToken(token));
    }
}

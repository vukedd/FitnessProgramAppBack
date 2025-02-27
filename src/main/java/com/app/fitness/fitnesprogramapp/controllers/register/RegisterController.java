package com.app.fitness.fitnesprogramapp.controllers.register;

import com.app.fitness.fitnesprogramapp.dtos.register.UserRegisterRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.register.UserRegisterResponseDto;
import com.app.fitness.fitnesprogramapp.services.register.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class RegisterController {
    private final RegisterService userRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDto> registerUser(@RequestBody UserRegisterRequestDto registrationRequest) {
        return ResponseEntity.ok(userRegistrationService.sendUserRegisterRequest(registrationRequest));
    }
}

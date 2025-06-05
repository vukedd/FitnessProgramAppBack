package com.app.fitness.fitnesprogramapp.controllers.register;

import com.app.fitness.fitnesprogramapp.dtos.register.UserRegisterRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.register.UserRegisterResponseDto;
import com.app.fitness.fitnesprogramapp.dtos.verification.UserVerificationResponseDto;
import com.app.fitness.fitnesprogramapp.exceptions.customExceptions.VerificationException;
import com.app.fitness.fitnesprogramapp.services.register.RegisterService;
import com.app.fitness.fitnesprogramapp.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.webauthn.api.UserVerificationRequirement;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

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
    public RedirectView verifyEmail(@RequestParam("token") String token) {
        int res = 200;

        try { userRegistrationService.validateVerificationToken(token);}
        catch (VerificationException e) {
            VerificationException.ErrorType exception = e.getErrorType();
            res = switch (exception.ordinal()) {
                case 0 -> 400;
                case 1 -> 409;
                default -> res;
            };
        }

        return new RedirectView("http://repzly.netlify.app/?status=" + String.valueOf(res));
    }
}

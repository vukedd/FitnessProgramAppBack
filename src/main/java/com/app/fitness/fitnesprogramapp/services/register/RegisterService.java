package com.app.fitness.fitnesprogramapp.services.register;

import com.app.fitness.fitnesprogramapp.dtos.register.UserRegisterRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.register.UserRegisterResponseDto;
import com.app.fitness.fitnesprogramapp.dtos.verification.UserVerificationResponseDto;
import com.app.fitness.fitnesprogramapp.exceptions.customExceptions.RegisterException;
import com.app.fitness.fitnesprogramapp.exceptions.customExceptions.VerificationException;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import com.app.fitness.fitnesprogramapp.services.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserRegisterResponseDto sendUserRegisterRequest(UserRegisterRequestDto registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RegisterException("E-mail is already in use!", RegisterException.ErrorType.EMAIL_ALREADY_IN_USE);
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RegisterException("Username is already in use!", RegisterException.ErrorType.USERNAME_ALREADY_IN_USE);
        }

        User user = User.builder().username(registerRequest.getUsername()).email(registerRequest.getEmail()).password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName()).lastName(registerRequest.getLastName()).enabled(false).build();


        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        user = userRepository.save(user);
        String confirmationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;
        emailService.sendEmail(user.getEmail(), "Email Verification", "Click the following link to verify your email: \n" + confirmationUrl);

        return new UserRegisterResponseDto(user.getId(), "User successfully registered!", user.getUsername(), user.getEmail(), user.getPassword());
    }

    public UserVerificationResponseDto validateVerificationToken(String token) {

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new VerificationException("Token not Found!", VerificationException.ErrorType.TOKEN_NOT_FOUND));

        if (user.isEnabled()) {
            throw new VerificationException("Account is already verified!", VerificationException.ErrorType.ALREADY_VERIFIED);
        }
        user.setEnabled(true);
        userRepository.save(user);

        return new UserVerificationResponseDto("Your account is now verified!");
    }
}

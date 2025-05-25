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
        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                "<div style='max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                "<h2 style='color: #333;'>Welcome, " + user.getFirstName() + "!</h2>" +
                "<p>Thank you for registering. Please verify your email address by clicking the button below:</p>" +
                "<p style='text-align: center; margin: 20px 0;'>" +
                "<a href=\"" + confirmationUrl + "\" " +
                "style=\"" +
                "background-color: #007bff; " +
                "color: white; " +
                "padding: 12px 25px; " +
                "text-align: center; " +
                "text-decoration: none; " +
                "display: inline-block; " +
                "border-radius: 5px; " +
                "font-size: 16px; " +
                "font-weight: bold; " +
                "border: none; " +
                "cursor: pointer;" +
                "\">" +
                "Verify Email Address" +
                "</a>" +
                "</p>" +
                "<p>If you're having trouble clicking the button, you can also copy and paste the following link into your web browser:</p>" +
                "<p><a href=\"" + confirmationUrl + "\">" + confirmationUrl + "</a></p>" +
                "<p>If you did not request this, please ignore this email.</p>" +
                "<p>Thanks,<br/>Your Repzly Team</p>" +
                "</div>" +
                "</body></html>";
        emailService.sendEmail(user.getEmail(), "Email Verification", htmlContent);

        return new UserRegisterResponseDto(user.getId(), "User successfully registered! We have sent a verification code to your email!", user.getUsername(), user.getEmail(), user.getPassword());
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

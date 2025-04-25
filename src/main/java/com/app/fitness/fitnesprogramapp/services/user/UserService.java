package com.app.fitness.fitnesprogramapp.services.user;
import com.app.fitness.fitnesprogramapp.dtos.user.PasswordChangeResponseDto;
import com.app.fitness.fitnesprogramapp.dtos.user.UserProfileDto;
import com.app.fitness.fitnesprogramapp.exceptions.customExceptions.PasswordChangeException;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.print.PrinterAbortException;

import static org.springframework.http.HttpStatus.GONE;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileDto getUserByUsername(final String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(GONE,
                        "The user account has been deleted or deactivated"));

        return new UserProfileDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

    public UserProfileDto getUserByRefreshTokenId(final Long refreshTokenId) {
        User user = userRepository.findUserByRefreshTokenId(refreshTokenId)
                .orElseThrow(() -> new ResponseStatusException(GONE,
                        "The user account has been deleted or deactivated"));

        return new UserProfileDto(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

    public PasswordChangeResponseDto editUserPassword(Long refreshTokenId, String oldPassword, String newPassword) {
        User user = userRepository.findUserByRefreshTokenId(refreshTokenId)
                .orElseThrow(() -> new ResponseStatusException(GONE,
                        "The user account has been deleted or deactivated"));

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new PasswordChangeException("Old password is incorrect!", PasswordChangeException.ErrorType.INVALID_CREDENTIALS);
        }

        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            throw new PasswordChangeException("The new password you have provided is too weak!", PasswordChangeException.ErrorType.INVALID_CREDENTIALS);
        }
//
        user.setPassword(passwordEncoder.encode(newPassword));
//
        userRepository.save(user);
//
        return new PasswordChangeResponseDto("Password has been changed successfully!");
//        return new PasswordChangeResponseDto()
    }
}
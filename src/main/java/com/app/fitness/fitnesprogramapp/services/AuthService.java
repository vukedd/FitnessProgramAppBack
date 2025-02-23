package com.app.fitness.fitnesprogramapp.services;

import com.app.fitness.fitnesprogramapp.dtos.auth.UserRegisterRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.auth.UserRegisterResponseDto;
import com.app.fitness.fitnesprogramapp.exceptions.RegisterException;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.repositories.auth.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository userRepository;

    public UserRegisterResponseDto sendUserRegisterRequest(UserRegisterRequestDto registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RegisterException("E-mail is already in use!", RegisterException.ErrorType.EMAIL_ALREADY_IN_USE);
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RegisterException("Username is already in use!", RegisterException.ErrorType.USERNAME_ALREADY_IN_USE);
        }

        User user = User.builder().username(registerRequest.getUsername()).email(registerRequest.getEmail()).password(registerRequest.getPassword())
                    .firstName(registerRequest.getFirstName()).lastName(registerRequest.getLastName()).build();

        user = userRepository.save(user);

        return new UserRegisterResponseDto(user.getId(), "User successfully registered!", user.getUsername(), user.getEmail(), user.getPassword(), user.getFirstName(), user.getLastName());
    }

}

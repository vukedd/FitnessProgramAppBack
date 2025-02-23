package com.app.fitness.fitnesprogramapp.services;

import com.app.fitness.fitnesprogramapp.dtos.auth.UserRegisterRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.auth.UserRegisterResponseDto;
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
            return new UserRegisterResponseDto(-1L, "Entered Email is already taken!", "", "", "", "", "");
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return new UserRegisterResponseDto(-1L, "Entered Username is already taken!", "", "", "", "", "");
        }

        User user = User.builder().username(registerRequest.getUsername()).email(registerRequest.getEmail()).password(registerRequest.getPassword())
                    .firstName(registerRequest.getFirstName()).lastName(registerRequest.getLastName()).build();

        user = userRepository.save(user);

        return new UserRegisterResponseDto(user.getId(), "User successfully registered!", user.getUsername(), user.getEmail(), user.getPassword(), user.getFirstName(), user.getLastName());
    }

}

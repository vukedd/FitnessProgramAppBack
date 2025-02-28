package com.app.fitness.fitnesprogramapp.services.auth;

import com.app.fitness.fitnesprogramapp.dtos.auth.AuthenticationRequestDto;
import com.app.fitness.fitnesprogramapp.dtos.auth.AuthenticationResponseDto;
import com.app.fitness.fitnesprogramapp.exceptions.customExceptions.LoginException;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthenticationResponseDto authenticate(
            final AuthenticationRequestDto request) {


        final var authToken = UsernamePasswordAuthenticationToken
                .unauthenticated(request.getUsername(), request.getPassword());

        final var authentication = authenticationManager.authenticate(authToken);

        final var user = (User) authentication.getPrincipal();
        userRepository.findVerifiedUserByUsername(user.getUsername()).orElseThrow(() -> new LoginException("User account is not verified!", LoginException.ErrorType.NOT_VERIFIED));

        final var token = jwtService.generateToken(request.getUsername());

        return new AuthenticationResponseDto(token);
    }
}

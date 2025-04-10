package com.app.fitness.fitnesprogramapp.services.user;
import com.app.fitness.fitnesprogramapp.dtos.user.UserProfileDto;
import com.app.fitness.fitnesprogramapp.dtos.volume.DailyWorkoutDurationReportDTO;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.GONE;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileDto getUserByUsername(final String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(GONE,
                        "The user account has been deleted or deactivated"));

        return new UserProfileDto(user.getUsername(), user.getEmail());
    }

    public UserProfileDto getUserByRefreshTokenId(final Long refreshTokenId) {
        User user = userRepository.findUserByRefreshTokenId(refreshTokenId)
                .orElseThrow(() -> new ResponseStatusException(GONE,
                        "The user account has been deleted or deactivated"));

        return new UserProfileDto(user.getUsername(), user.getEmail());
    }
}
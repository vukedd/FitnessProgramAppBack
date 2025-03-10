package com.app.fitness.fitnesprogramapp.controllers.user;

import com.app.fitness.fitnesprogramapp.dtos.user.UserProfileDto;
import com.app.fitness.fitnesprogramapp.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getUserByUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
}

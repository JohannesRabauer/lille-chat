package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.service.UserService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDto getCurrentUser(JwtAuthenticationToken token) {
        return userService.getOrCreateUser(token);
    }

    @GetMapping("/search")
    public List<UserDto> searchUsers(@RequestParam("q") String query) {
        return userService.searchByUsername(query);
    }
}

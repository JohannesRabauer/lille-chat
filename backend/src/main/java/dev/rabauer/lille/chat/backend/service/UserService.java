package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.UserDto;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto getOrCreateUser(JwtAuthenticationToken token);

    UserDto findById(UUID id);

    List<UserDto> searchByUsername(String query);
}

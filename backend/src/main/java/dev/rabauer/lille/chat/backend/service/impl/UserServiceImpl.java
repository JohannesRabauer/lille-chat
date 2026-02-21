package dev.rabauer.lille.chat.backend.service.impl;

import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.UserEntity;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import dev.rabauer.lille.chat.backend.service.UserService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto getOrCreateUser(JwtAuthenticationToken token) {
        UUID id = UUID.fromString(token.getToken().getClaimAsString("sub"));
        String username = token.getToken().getClaimAsString("preferred_username");
        String email = token.getToken().getClaimAsString("email");

        return userRepository.findById(id)
                .map(this::toDto)
                .orElseGet(() -> {
                    UserEntity user = new UserEntity();
                    user.setId(id);
                    user.setUsername(username);
                    user.setDisplayName(username);
                    user.setEmail(email);
                    return toDto(userRepository.save(user));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(UUID id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> searchByUsername(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private UserDto toDto(UserEntity entity) {
        return new UserDto(entity.getId(), entity.getUsername(), entity.getDisplayName(), entity.getEmail());
    }
}

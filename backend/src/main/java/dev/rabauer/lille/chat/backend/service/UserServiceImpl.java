package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.UserEntity;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto getOrCreateUser(JwtAuthenticationToken token) {
        UUID sub = UUID.fromString(token.getToken().getClaimAsString("sub"));
        String username = token.getToken().getClaimAsString("preferred_username");
        String email = token.getToken().getClaimAsString("email");

        UserEntity user = userRepository.findById(sub).orElseGet(() -> {
            UserEntity newUser = new UserEntity();
            newUser.setId(sub);
            newUser.setUsername(username);
            newUser.setDisplayName(username);
            newUser.setEmail(email);
            return userRepository.save(newUser);
        });

        return toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
        return toDto(user);
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
        return new UserDto(entity.getId(), entity.getUsername(),
                entity.getDisplayName(), entity.getEmail());
    }
}

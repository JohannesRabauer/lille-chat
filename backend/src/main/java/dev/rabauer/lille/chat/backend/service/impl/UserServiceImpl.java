package dev.rabauer.lille.chat.backend.service.impl;

import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.User;
import dev.rabauer.lille.chat.backend.exception.UserNotFoundException;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import dev.rabauer.lille.chat.backend.service.UserService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDto getOrCreateUser(JwtAuthenticationToken token) {
    Jwt jwt = token.getToken();

    // Get subject - try multiple approaches for compatibility
    String subject = jwt.getSubject();
    if (subject == null) {
      subject = jwt.getClaimAsString("sub");
    }
    if (subject == null) {
      subject = token.getName();
    }

    // Get username
    String username = jwt.getClaimAsString("preferred_username");
    if (username == null) {
      username = jwt.getClaimAsString("username");
    }

    // Get email
    String email = jwt.getClaimAsString("email");

    // If still no subject, generate deterministic UUID from email or username
    UUID id;
    if (subject == null || subject.isBlank()) {
      String identifier = email != null ? email : username;
      if (identifier == null) {
        logger.error("JWT token has no subject, email, or username. Claims: {}", jwt.getClaims());
        throw new IllegalStateException("JWT token missing required identity claims");
      }
      // Generate deterministic UUID from email/username using UUID v5 (name-based)
      id = UUID.nameUUIDFromBytes(identifier.getBytes(StandardCharsets.UTF_8));
      logger.warn("JWT missing sub claim, generated UUID from {}: {}", identifier, id);
    } else {
      id = UUID.fromString(subject);
    }

    // Set fallbacks
    if (username == null) {
      username = subject != null ? subject : email;
    }
    if (email == null) {
      email = username + "@unknown.local";
    }

    final String finalUsername = username;
    final String finalEmail = email;

    User user = userRepository.findById(id)
        .orElseGet(() -> {
          User newUser = new User(id, finalUsername, finalUsername, finalEmail);
          return userRepository.save(newUser);
        });

    return toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto findById(UUID id) {
    return userRepository.findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new UserNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserDto> searchByUsername(String query) {
    return userRepository.searchByUsername(query).stream()
        .map(this::toDto)
        .toList();
  }

  private UserDto toDto(User user) {
    return new UserDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail());
  }
}

package dev.rabauer.lille.chat.backend;

import java.time.Instant;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestSecurityConfig {

  @Bean
  @Primary
  public JwtDecoder jwtDecoder() {
    // Return a mock JwtDecoder that creates a Jwt from the token value
    // In tests, we use mock JwtAuthenticationToken directly, so this is just a fallback
    return token -> Jwt.withTokenValue(token)
        .header("alg", "none")
        .claim("sub", "test-user")
        .claim("preferred_username", "testuser")
        .claim("email", "test@example.com")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
  }
}

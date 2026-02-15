package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.TestcontainersConfiguration;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class UserServiceTest {

    @Autowired
    UserService userService;

    private JwtAuthenticationToken buildToken(UUID sub, String username, String email) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", sub.toString())
                .claim("preferred_username", username)
                .claim("email", email)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    @Test
    void getOrCreateUser_createsNewUser() {
        UUID sub = UUID.randomUUID();
        JwtAuthenticationToken token = buildToken(sub, "testuser", "test@lille-chat.dev");

        UserDto user = userService.getOrCreateUser(token);

        assertThat(user).isNotNull();
        assertThat(user.id()).isEqualTo(sub);
        assertThat(user.username()).isEqualTo("testuser");
        assertThat(user.email()).isEqualTo("test@lille-chat.dev");
    }

    @Test
    void getOrCreateUser_returnsSameUserOnSecondCall() {
        UUID sub = UUID.randomUUID();
        JwtAuthenticationToken token = buildToken(sub, "repeat", "repeat@lille-chat.dev");

        UserDto first = userService.getOrCreateUser(token);
        UserDto second = userService.getOrCreateUser(token);

        assertThat(first.id()).isEqualTo(second.id());
    }

    @Test
    void findById_returnsExistingUser() {
        UUID sub = UUID.randomUUID();
        JwtAuthenticationToken token = buildToken(sub, "findme", "findme@lille-chat.dev");
        userService.getOrCreateUser(token);

        UserDto found = userService.findById(sub);

        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo(sub);
    }

    @Test
    void findById_throwsForUnknownId() {
        assertThatThrownBy(() -> userService.findById(UUID.randomUUID()))
                .isInstanceOf(Exception.class);
    }

    @Test
    void searchByUsername_findsMatchingUsers() {
        UUID sub = UUID.randomUUID();
        JwtAuthenticationToken token = buildToken(sub, "searchable", "search@lille-chat.dev");
        userService.getOrCreateUser(token);

        List<UserDto> results = userService.searchByUsername("searcha");

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(u -> u.username().equals("searchable"));
    }

    @Test
    void searchByUsername_returnsEmptyForNoMatch() {
        List<UserDto> results = userService.searchByUsername("nonexistent_xyz_999");

        assertThat(results).isEmpty();
    }
}

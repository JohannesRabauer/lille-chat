package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    List<UserEntity> findByUsernameContainingIgnoreCase(String query);
}

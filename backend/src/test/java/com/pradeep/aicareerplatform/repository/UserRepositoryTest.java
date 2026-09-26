package com.pradeep.aicareerplatform.repository;

import com.pradeep.aicareerplatform.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveUser() {

        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@gmail.com");
        user.setPassword("encodedPassword");

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFullName()).isEqualTo("Test User");
        assertThat(savedUser.getEmail()).isEqualTo("test@gmail.com");
    }

    @Test
    void shouldFindUserByEmail() {

        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@gmail.com");
        user.setPassword("encodedPassword");

        userRepository.save(user);

        Optional<User> result =
                userRepository.findByEmail("test@gmail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName())
                .isEqualTo("Test User");
        assertThat(result.get().getEmail())
                .isEqualTo("test@gmail.com");
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {

        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@gmail.com");
        user.setPassword("encodedPassword");

        userRepository.save(user);

        boolean exists =
                userRepository.existsByEmail("test@gmail.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {

        boolean exists =
                userRepository.existsByEmail("unknown@gmail.com");

        assertThat(exists).isFalse();
    }
}
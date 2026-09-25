package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.dto.AuthResponseDto;
import com.pradeep.aicareerplatform.dto.LoginRequestDto;
import com.pradeep.aicareerplatform.dto.RegisterRequestDto;
import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.repository.UserRepository;
import com.pradeep.aicareerplatform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests — UserRepository, PasswordEncoder and JwtService are all
 * mocked, so these run in milliseconds with no database or Spring context
 * required. This is intentional: it tests AuthService's own logic
 * (duplicate-email check, password verification, token generation call)
 * in isolation, not the whole stack.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto();
        registerRequest.setFullName("Pradeep Bhat");
        registerRequest.setEmail("pradeep@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("pradeep@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void register_savesNewUser_andReturnsTokenWithHashedPassword() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashed-password");
        when(jwtService.generateToken(registerRequest.getEmail())).thenReturn("fake-jwt-token");

        AuthResponseDto response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getFullName()).isEqualTo("Pradeep Bhat");
        assertThat(response.getEmail()).isEqualTo("pradeep@example.com");

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("pradeep@example.com") &&
                        user.getPassword().equals("hashed-password")
        ));
    }

    @Test
    void register_rejectsDuplicateEmail_andNeverSavesOrHashesPassword() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_returnsToken_whenCredentialsAreCorrect() {
        User existingUser = new User();
        existingUser.setEmail("pradeep@example.com");
        existingUser.setFullName("Pradeep Bhat");
        existingUser.setPassword("hashed-password");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(existingUser.getEmail())).thenReturn("fake-jwt-token");

        AuthResponseDto response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getEmail()).isEqualTo("pradeep@example.com");
    }

    @Test
    void login_rejectsWrongPassword_withGenericMessage() {
        User existingUser = new User();
        existingUser.setEmail("pradeep@example.com");
        existingUser.setPassword("hashed-password");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_rejectsUnknownEmail_withSameGenericMessageAsWrongPassword() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }
}
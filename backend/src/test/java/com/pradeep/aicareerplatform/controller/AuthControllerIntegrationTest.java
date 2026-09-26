package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.exception.GlobalExceptionHandler;
import com.pradeep.aicareerplatform.repository.UserRepository;
import com.pradeep.aicareerplatform.security.AuthCookieUtil;
import com.pradeep.aicareerplatform.security.JwtService;
import com.pradeep.aicareerplatform.security.RateLimiterService;
import com.pradeep.aicareerplatform.service.AuthService;
import com.pradeep.aicareerplatform.service.OtpService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AuthService.class, GlobalExceptionHandler.class})
class AuthControllerIntegrationTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @MockitoBean
    private AuthCookieUtil authCookieUtil;



    @org.springframework.beans.factory.annotation.Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterUserThroughControllerAndService() throws Exception {

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password@123"))
                .thenReturn("encodedPassword");

        when(jwtService.generateToken("test@gmail.com"))
                .thenReturn("test-jwt-token");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseCookie cookie = ResponseCookie
                .from("access_token", "test-jwt-token")
                .httpOnly(true)
                .path("/")
                .build();

        when(authCookieUtil.buildLoginCookie("test-jwt-token"))
                .thenReturn(cookie);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                    "fullName": "Test User",
                                    "email": "test@gmail.com",
                                    "password": "Password@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(header().exists("Set-Cookie"));

        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken("test@gmail.com");
    }

    @Test
    void shouldLoginUserThroughControllerAndService() throws Exception {

        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@gmail.com");
        user.setPassword("encodedPassword");

        when(rateLimiterService.tryAcquire(
                eq("login:test@gmail.com"),
                eq(5),
                eq(900L)
        )).thenReturn(true);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches("Password@123", "encodedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken("test@gmail.com"))
                .thenReturn("test-jwt-token");

        ResponseCookie cookie = ResponseCookie
                .from("access_token", "test-jwt-token")
                .httpOnly(true)
                .path("/")
                .build();

        when(authCookieUtil.buildLoginCookie("test-jwt-token"))
                .thenReturn(cookie);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                    "email": "test@gmail.com",
                                    "password": "Password@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(header().exists("Set-Cookie"));

        verify(userRepository).findByEmail("test@gmail.com");
        verify(passwordEncoder).matches(
                "Password@123",
                "encodedPassword"
        );
        verify(jwtService).generateToken("test@gmail.com");
        verify(rateLimiterService).reset("login:test@gmail.com");
    }

    @Test
    void shouldReturnBadRequestForInvalidLogin() throws Exception {

        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@gmail.com");
        user.setPassword("encodedPassword");

        when(rateLimiterService.tryAcquire(
                eq("login:test@gmail.com"),
                eq(5),
                eq(900L)
        )).thenReturn(true);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches("WrongPassword", "encodedPassword"))
                .thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                    "email": "test@gmail.com",
                                    "password": "WrongPassword"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));

        verify(passwordEncoder).matches(
                "WrongPassword",
                "encodedPassword"
        );

        verify(jwtService, never())
                .generateToken(any());

        verify(rateLimiterService, never())
                .reset("login:test@gmail.com");
    }
}
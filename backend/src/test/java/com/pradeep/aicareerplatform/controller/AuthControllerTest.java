package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.AuthResponseDto;
import com.pradeep.aicareerplatform.dto.LoginRequestDto;
import com.pradeep.aicareerplatform.dto.RegisterRequestDto;
import com.pradeep.aicareerplatform.repository.UserRepository;
import com.pradeep.aicareerplatform.security.AuthCookieUtil;
import com.pradeep.aicareerplatform.security.JwtService;
import com.pradeep.aicareerplatform.security.RateLimiterService;
import com.pradeep.aicareerplatform.service.AuthService;
import com.pradeep.aicareerplatform.service.OtpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @MockitoBean
    private AuthCookieUtil authCookieUtil;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void register_returns200AndSetsCookie_whenRequestIsValid() throws Exception {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponseDto response = new AuthResponseDto(
                "fake-jwt-token",
                "Test User",
                "test@example.com"
        );

        ResponseCookie cookie = ResponseCookie.from(
                        "AUTH_TOKEN",
                        "fake-jwt-token"
                )
                .httpOnly(true)
                .path("/")
                .build();

        when(authService.register(any(RegisterRequestDto.class)))
                .thenReturn(response);

        when(authCookieUtil.buildLoginCookie("fake-jwt-token"))
                .thenReturn(cookie);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "fullName": "Test User",
                                            "email": "test@example.com",
                                            "password": "password123"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void login_returns200AndSetsCookie_whenCredentialsAreValid() throws Exception {

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponseDto response = new AuthResponseDto(
                "fake-jwt-token",
                "Test User",
                "test@example.com"
        );

        ResponseCookie cookie = ResponseCookie.from(
                        "AUTH_TOKEN",
                        "fake-jwt-token"
                )
                .httpOnly(true)
                .path("/")
                .build();

        when(rateLimiterService.tryAcquire(
                anyString(),
                any(Integer.class),
                anyLong()
        )).thenReturn(true);

        when(authService.login(any(LoginRequestDto.class)))
                .thenReturn(response);

        when(authCookieUtil.buildLoginCookie("fake-jwt-token"))
                .thenReturn(cookie);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "test@example.com",
                                            "password": "password123"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void login_returns429_whenRateLimitIsExceeded() throws Exception {

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(rateLimiterService.tryAcquire(
                anyString(),
                any(Integer.class),
                anyLong()
        )).thenReturn(false);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "test@example.com",
                                            "password": "password123"
                                        }
                                        """)
                )
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void register_rejectsInvalidRequest() throws Exception {

        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("");
        request.setEmail("invalid-email");
        request.setPassword("");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "fullName": "",
                                            "email": "invalid-email",
                                            "password": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_rejectsInvalidRequest() throws Exception {

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("invalid-email");
        request.setPassword("");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "invalid-email",
                                            "password": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}
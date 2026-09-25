package com.pradeep.aicareerplatform.security;

import com.pradeep.aicareerplatform.controller.AuthController;
import com.pradeep.aicareerplatform.dto.AuthResponseDto;
import com.pradeep.aicareerplatform.dto.LoginRequestDto;
import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.repository.UserRepository;
import com.pradeep.aicareerplatform.service.AuthService;
import com.pradeep.aicareerplatform.service.OtpService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
@TestPropertySource(properties = {
        "jwt.secret=ThisIsAVeryStrongSecretKeyForTesting123456789",
        "jwt.expiration=3600000"
})
class JwtSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

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

    @Test
    void protectedEndpoint_rejectsRequestWithoutJwt() throws Exception {

        mockMvc.perform(
                        get("/api/auth/me")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_allowsRequestWithValidJwt() throws Exception {

        String email = "test@example.com";

        String token = jwtService.generateToken(email);

        User user = new User();
        user.setEmail(email);
        user.setFullName("Test User");

        when(userRepository.findByEmail(email))
                .thenReturn(java.util.Optional.of(user));

        mockMvc.perform(
                        get("/api/auth/me")
                                .cookie(new Cookie(
                                        AuthCookieUtil.COOKIE_NAME,
                                        token
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void protectedEndpoint_rejectsRequestWithInvalidJwt() throws Exception {

        mockMvc.perform(
                        get("/api/auth/me")
                                .cookie(new Cookie(
                                        AuthCookieUtil.COOKIE_NAME,
                                        "invalid.jwt.token"
                                ))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void loginEndpoint_isAccessibleWithoutJwt() throws Exception {

        AuthResponseDto response = new AuthResponseDto(
                "fake-jwt-token",
                "Test User",
                "test@example.com"
        );

        ResponseCookie cookie = ResponseCookie.from(
                AuthCookieUtil.COOKIE_NAME,
                "fake-jwt-token"
        ).build();

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
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }
}
package com.pradeep.aicareerplatform.controller;

import com.pradeep.aicareerplatform.dto.*;
import com.pradeep.aicareerplatform.entity.User;
import com.pradeep.aicareerplatform.exception.TooManyRequestsException;
import com.pradeep.aicareerplatform.repository.UserRepository;
import com.pradeep.aicareerplatform.security.AuthCookieUtil;
import com.pradeep.aicareerplatform.security.RateLimiterService;
import com.pradeep.aicareerplatform.service.AuthService;
import com.pradeep.aicareerplatform.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiterService rateLimiterService;
    private final AuthCookieUtil authCookieUtil;

    private static final int LOGIN_MAX_ATTEMPTS = 5;
    private static final long LOGIN_WINDOW_SECONDS = 15 * 60;

    private static final int FORGOT_PASSWORD_MAX_ATTEMPTS = 3;
    private static final long FORGOT_PASSWORD_WINDOW_SECONDS = 15 * 60;

    private static final int RESET_PASSWORD_MAX_ATTEMPTS = 5;
    private static final long RESET_PASSWORD_WINDOW_SECONDS = 15 * 60;

    public AuthController(AuthService authService, UserRepository userRepository, OtpService otpService,
                          PasswordEncoder passwordEncoder, RateLimiterService rateLimiterService,
                          AuthCookieUtil authCookieUtil) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiterService = rateLimiterService;
        this.authCookieUtil = authCookieUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        ResponseCookie cookie = authCookieUtil.buildLoginCookie(response.getToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        String rateLimitKey = "login:" + request.getEmail().toLowerCase();
        if (!rateLimiterService.tryAcquire(rateLimitKey, LOGIN_MAX_ATTEMPTS, LOGIN_WINDOW_SECONDS)) {
            throw new TooManyRequestsException("Too many login attempts. Please try again in a few minutes.");
        }

        AuthResponseDto response = authService.login(request);
        rateLimiterService.reset(rateLimitKey);

        ResponseCookie cookie = authCookieUtil.buildLoginCookie(response.getToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie cookie = authCookieUtil.buildLogoutCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out");
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(java.security.Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(Map.of("email", user.getEmail(), "fullName", user.getFullName()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        String email = request.getEmail().toLowerCase();
        String rateLimitKey = "forgot-password:" + email;
        if (!rateLimiterService.tryAcquire(rateLimitKey, FORGOT_PASSWORD_MAX_ATTEMPTS, FORGOT_PASSWORD_WINDOW_SECONDS)) {
            throw new TooManyRequestsException("Too many requests. Please try again in a few minutes.");
        }

        if (userRepository.existsByEmail(email)) {
            otpService.generateAndSendOtp(email);
        }

        return ResponseEntity.ok("If an account with that email exists, we've sent a reset code.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        String email = request.getEmail().toLowerCase();
        String rateLimitKey = "reset-password:" + email;
        if (!rateLimiterService.tryAcquire(rateLimitKey, RESET_PASSWORD_MAX_ATTEMPTS, RESET_PASSWORD_WINDOW_SECONDS)) {
            throw new TooManyRequestsException("Too many attempts. Please try again in a few minutes.");
        }

        boolean isValid = otpService.verifyOtp(email, request.getOtpCode());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        rateLimiterService.reset(rateLimitKey);

        return ResponseEntity.ok("Password reset successful");
    }
}
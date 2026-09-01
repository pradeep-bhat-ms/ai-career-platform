package com.pradeep.aicareerplatform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieUtil {

    public static final String COOKIE_NAME = "access_token";

    @Value("${app.cookie.secure:false}")
    private boolean secure;

    private final JwtService jwtService;

    public AuthCookieUtil(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public ResponseCookie buildLoginCookie(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure) // false in dev (HTTP), true in prod (HTTPS)
                .sameSite(secure ? "None" : "Lax") // "None" + Secure=true for cross-origin HTTPS
                .path("/")
                .maxAge(jwtService.getExpirationMs() / 1000)
                .build();
    }

    public ResponseCookie buildLogoutCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(secure ? "None" : "Lax")
                .path("/")
                .maxAge(0)
                .build();
    }
}
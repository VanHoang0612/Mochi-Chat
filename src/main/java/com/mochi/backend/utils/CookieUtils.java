package com.mochi.backend.utils;


import com.mochi.backend.enums.ErrorCode;
import com.mochi.backend.exception.AppException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    @Value("${security.jwt.expiration-ms.refresh}")
    private long refreshExpirationMs;

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        int maxAgeInSeconds = (int) (refreshExpirationMs / 1000);
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .sameSite("Lax")
                .build();
    }

    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName()
                        .equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        throw new AppException(ErrorCode.NOT_FOUND_IN_COOKIES);

    }


}
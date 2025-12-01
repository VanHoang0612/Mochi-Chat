package com.mochi.backend.controller;

import com.mochi.backend.dto.api.ApiResponse;
import com.mochi.backend.dto.auth.AuthResponse;
import com.mochi.backend.dto.auth.LoginRequest;
import com.mochi.backend.dto.auth.RefreshTokenResponse;
import com.mochi.backend.dto.auth.RegisterRequest;
import com.mochi.backend.enums.SuccessCode;
import com.mochi.backend.security.email.dto.ReSendVerificationCodeRequest;
import com.mochi.backend.security.email.dto.VerifyEmailRequest;
import com.mochi.backend.service.AuthService;
import com.mochi.backend.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${security.jwt.expiration-ms.refresh}")
    private long refreshExpirationMs;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(SuccessCode.REGISTER.getStatus())
                .body(ApiResponse.success(null, SuccessCode.REGISTER));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@Valid @RequestBody VerifyEmailRequest verifyEmailRequest) {
        authService.verifyEmail(verifyEmailRequest);
        return ResponseEntity.status(SuccessCode.VERIFY_EMAIL.getStatus())
                .body(ApiResponse.success(null, SuccessCode.VERIFY_EMAIL));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<ApiResponse<?>> resendVerificationCode(@Valid @RequestBody ReSendVerificationCodeRequest request) {
        authService.reSendVerificationCode(request);
        return ResponseEntity.status(SuccessCode.RESEND_VERIFICATION_CODE.getStatus())
                .body(ApiResponse.success(null, SuccessCode.RESEND_VERIFICATION_CODE));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        int maxAgeInSeconds = (int) (refreshExpirationMs / 1000);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .sameSite("Lax")
                .build();
        authResponse.setRefreshToken(null);
        return ResponseEntity.status(SuccessCode.LOGIN.getStatus())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success(authResponse, SuccessCode.LOGIN));

    }


    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(HttpServletRequest request) {
        String refreshToken = CookieUtils.getCookieValue(request, "refreshToken");
        return ResponseEntity.status(SuccessCode.REFRESH_TOKEN.getStatus())
                .body(ApiResponse.success(authService.refreshToken(refreshToken), SuccessCode.REFRESH_TOKEN));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        ResponseCookie refreshToken = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshToken.toString())
                .build();
    }

}

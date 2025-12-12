package com.mochi.backend.controller;

import com.mochi.backend.dto.api.ApiResponse;
import com.mochi.backend.dto.auth.*;
import com.mochi.backend.enums.SuccessCode;
import com.mochi.backend.service.AuthService;
import com.mochi.backend.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieUtils cookieUtils;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(SuccessCode.REGISTER.getStatus())
                .body(ApiResponse.success(null, SuccessCode.REGISTER));
    }

    // xac thuc email, kich hoat tai khoan
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@Valid @RequestBody VerifyEmailRequest verifyEmailRequest) {
        authService.verifyEmail(verifyEmailRequest);
        return ResponseEntity.status(SuccessCode.VERIFY_EMAIL.getStatus())
                .body(ApiResponse.success(null, SuccessCode.VERIFY_EMAIL));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<ApiResponse<?>> resendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request) {
        authService.reSendVerificationCode(request);
        return ResponseEntity.status(SuccessCode.RESEND_VERIFICATION_CODE.getStatus())
                .body(ApiResponse.success(null, SuccessCode.RESEND_VERIFICATION_CODE));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        ResponseCookie refreshTokenCookie = cookieUtils.createRefreshTokenCookie(authResponse.getRefreshToken());
        authResponse.setRefreshToken(null);
        return ResponseEntity.status(SuccessCode.LOGIN.getStatus())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success(authResponse, SuccessCode.LOGIN));

    }

//    @GetMapping("/login-google-success")
//    public ResponseEntity<ApiResponse<AuthResponse>> loginGoogleSuccess(OAuth2AuthenticationToken authentication) {
//        Map<String, Object> attributes = authentication.getPrincipal()
//                .getAttributes();
//        AuthResponse authResponse = authService.loginGoogleSuccess(attributes);
//        ResponseCookie refreshTokenCookie = cookieUtils.createRefreshTokenCookie(authResponse.getRefreshToken());
//        authResponse.setRefreshToken(null);
//        return ResponseEntity.status(SuccessCode.LOGIN.getStatus())
//                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
//                .body(ApiResponse.success(authResponse, SuccessCode.LOGIN));
//    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody SendVerificationCodeRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.status(SuccessCode.SUCCESS.getStatus())
                .body(ApiResponse.success(null, SuccessCode.SUCCESS));

    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.status(SuccessCode.VERIFY_OTP.getStatus())
                .body(ApiResponse.success(authService.verifyOtp(request), SuccessCode.VERIFY_OTP));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.status(SuccessCode.SUCCESS.getStatus())
                .body(ApiResponse.success(null, SuccessCode.SUCCESS));
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

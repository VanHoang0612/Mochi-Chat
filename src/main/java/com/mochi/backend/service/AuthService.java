package com.mochi.backend.service;

import com.mochi.backend.dto.auth.*;
import com.mochi.backend.enums.ErrorCode;
import com.mochi.backend.enums.RoleType;
import com.mochi.backend.exception.AppException;
import com.mochi.backend.mapper.UserMapper;
import com.mochi.backend.model.RevokedToken;
import com.mochi.backend.model.User;
import com.mochi.backend.security.email.EmailService;
import com.mochi.backend.security.jwt.JwtService;
import com.mochi.backend.security.userDetails.CustomUserDetails;
import com.mochi.backend.security.userDetails.CustomUserDetailsService;
import com.mochi.backend.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RevokedTokenService revokedTokenService;
    private final OtpService otpService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {

        if (userService.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_REGISTERED);
        }
        if (userService.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_REGISTERED);
        }
        User user = User.builder()
                .username(request.getUsername()
                        .trim())
                .email(request.getEmail()
                        .trim())
                .password(passwordEncoder.encode(request.getPassword()
                        .trim()))
                .firstname(request.getFirstname()
                        .trim())
                .lastname(request.getLastname()
                        .trim())
                .roles(
                        Set.of(roleService.findByName(RoleType.ROLE_USER.name())
                                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)))
                )
                .build();

        try {
            userService.saveUser(user);
            emailService.sendVerificationEmail(user.getEmail(), otpService.generateCode(user.getEmail()));
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER, List.of(e.getMessage()));
        }
    }

    public void verifyEmail(VerifyEmailRequest request) {
        Optional<User> optionalUser = userService.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (otpService.verifyOtp(user.getEmail(), request.getVerificationCode())) {
                user.setEnabled(true);
                userService.saveUser(user);
            }
        } else {
            throw new AppException(ErrorCode.EMAIL_NOT_REGISTERED);
        }
    }

    public VerifyOtpResponse verifyOtp(@Valid VerifyEmailRequest request) {
        Optional<User> optionalUser = userService.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            otpService.verifyOtp(optionalUser.get()
                    .getEmail(), request.getVerificationCode());
            String resetToken = UUID.randomUUID()
                    .toString();
            redisService.saveValue("resetToken:" + resetToken, optionalUser.get()
                    .getEmail());
            return VerifyOtpResponse.builder()
                    .resetToken(resetToken)
                    .build();
        } else {
            throw new AppException(ErrorCode.EMAIL_NOT_REGISTERED);
        }
    }

    public void reSendVerificationCode(SendVerificationCodeRequest request) {
        Optional<User> optionalUser = userService.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new AppException(ErrorCode.USER_IS_ENABLED);
            }
            emailService.sendVerificationEmail(user.getEmail(), otpService.generateCode(user.getEmail()));
        } else {
            throw new AppException(ErrorCode.EMAIL_NOT_REGISTERED);
        }
    }


    public AuthResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext()
                    .setAuthentication(auth);
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userDetails.getUser();
            System.out.println(SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getAuthorities());
            return AuthResponse.builder()
                    .accessToken(jwtService.generateAccessToken(userDetails))
                    .expiresInMS(jwtService.getAccessTokenExpiry())
                    .refreshToken(jwtService.generateRefreshToken(userDetails))
                    .user(
                            userMapper.toDto(user)
                    )

                    .build();
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.LOGIN_FAIL);

        } catch (DisabledException e) {
            throw new AppException(ErrorCode.USER_DISABLE);
        }
    }


    public RefreshTokenResponse refreshToken(String token) {
        if (!jwtService.isRefreshTokenValid(token)) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        String username = jwtService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return RefreshTokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(userDetails))
                .expiresInMS(jwtService.getAccessTokenExpiry())
                .build();
    }

    public void logout(HttpServletRequest request) {
        String refreshToken = CookieUtils.getCookieValue(request, "refreshToken");
        if (refreshToken == null || !jwtService.isRefreshTokenValid(refreshToken)) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        UUID jti = jwtService.extractJti(refreshToken);
        Date expiryDate = jwtService.extractExpiration(refreshToken);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(expiryDate.toInstant(), TimeZone.getDefault()
                .toZoneId());
        if (revokedTokenService.existsByJti(jti)) {
            throw new AppException(ErrorCode.TOKEN_EXISTS);
        }
        revokedTokenService.save(
                RevokedToken.builder()
                        .jti(jti)
                        .expiresAt(expiresAt)
                        .revokedAt(LocalDateTime.now())
                        .build()
        );
        SecurityContextHolder.clearContext();
    }

    public void forgotPassword(SendVerificationCodeRequest request) {
        Optional<User> optionalUser = userService.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            emailService.sendVerificationEmail(user.getEmail(), otpService.generateCode(user.getEmail()));
        } else {
            throw new AppException(ErrorCode.EMAIL_NOT_REGISTERED);
        }
    }


    public void resetPassword(@Valid ResetPasswordRequest request) {
        String storedEmail = redisService.getValue("resetToken:" + request.getResetToken());
        if (storedEmail == null) {
            throw new AppException(ErrorCode.INVALID_RESET_TOKEN);
        }
        Optional<User> optionalUser = userService.findByEmail(storedEmail);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userService.saveUser(user);
            redisService.deleteValue("resetToken:" + request.getResetToken());
        } else {
            throw new AppException(ErrorCode.EMAIL_NOT_REGISTERED);
        }

    }
}

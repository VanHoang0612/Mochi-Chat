package com.mochi.backend.auth;

import com.mochi.backend.RevokedToken.RevokedToken;
import com.mochi.backend.RevokedToken.RevokedTokenService;
import com.mochi.backend.auth.dto.AuthResponse;
import com.mochi.backend.auth.dto.LoginRequest;
import com.mochi.backend.auth.dto.RefreshTokenResponse;
import com.mochi.backend.auth.dto.RegisterRequest;
import com.mochi.backend.exception.AppException;
import com.mochi.backend.role.RoleService;
import com.mochi.backend.security.email.EmailService;
import com.mochi.backend.security.email.dto.ReSendVerificationCodeRequest;
import com.mochi.backend.security.email.dto.VerifyEmailRequest;
import com.mochi.backend.security.jwt.JwtService;
import com.mochi.backend.security.userDetails.CustomUserDetails;
import com.mochi.backend.security.userDetails.CustomUserDetailsService;
import com.mochi.backend.shared.enums.ErrorCode;
import com.mochi.backend.shared.enums.RoleType;
import com.mochi.backend.shared.mapper.UserMapper;
import com.mochi.backend.shared.utils.CookieUtils;
import com.mochi.backend.user.User;
import com.mochi.backend.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    public void register(RegisterRequest request) {

        if (userService.existsByEmail(request.getEmail()) || userService.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.ACCOUNT_REGISTERED);
        }
        User user = User.builder()
                .username(request.getUsername()
                        .trim())
                .email(request.getEmail()
                        .trim())
                .password(request.getPassword()
                        .trim())
                .firstname(request.getFirstname()
                        .trim())
                .lastname(request.getLastname()
                        .trim())
                .roles(
                        Set.of(roleService.findByName(RoleType.ROLE_USER.name())
                                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)))
                )
                .verificationCode(generateVerificationCode())
                .verificationExpiresAt(LocalDateTime.now()
                        .plusMinutes(10))
                .build();

        try {
            userService.saveUser(user);
            sendVerificationEmail(user);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER, List.of(e.getMessage()));
        }
    }

    public void verifyEmail(VerifyEmailRequest request) {
        Optional<User> optionalUser = userService.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationExpiresAt()
                    .isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.VERIFICATION_CODE_EXPIRED);
            }
            if (user.getVerificationCode()
                    .equals(request.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationExpiresAt(null);
                userService.saveUser(user);
            } else {
                throw new AppException(ErrorCode.VERIFICATION_CODE_INVALID);
            }
        } else {
            throw new AppException(ErrorCode.EMAIL_NOT_REGISTERED);
        }
    }

    public void reSendVerificationCode(ReSendVerificationCodeRequest request) {
        Optional<User> optionalUser = userService.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new AppException(ErrorCode.USER_IS_ENABLED);
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationExpiresAt(LocalDateTime.now()
                    .plusMinutes(15));
            sendVerificationEmail(user);
            userService.saveUser(user);
        } else {
            throw new AppException(ErrorCode.EMAIL_NOT_REGISTERED);
        }
    }

    private void sendVerificationEmail(User user) {
        String subject = "Verify your email";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAIL, List.of(e.getMessage()));
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
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
}

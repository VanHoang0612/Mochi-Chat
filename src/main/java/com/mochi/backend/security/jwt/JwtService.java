package com.mochi.backend.security.jwt;

import com.mochi.backend.enums.ErrorCode;
import com.mochi.backend.exception.AppException;
import com.mochi.backend.model.RevokedToken;
import com.mochi.backend.service.RevokedTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {
    private final RevokedTokenService revokedTokenService;
    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${security.jwt.expiration-ms.access}")
    private long accessExpirationMs;

    @Value("${security.jwt.expiration-ms.refresh}")
    private long refreshExpirationMs;

    public JwtService(RevokedTokenService revokedTokenService) {
        this.revokedTokenService = revokedTokenService;
    }

    //    trich xuat cac claim tu token tra ve kieu T
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //    trich xuat tat ca claims tu token
    public Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSingInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("Token đã hết hạn: {}", e.getMessage());
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất claims từ token: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public UUID extractJti(String token) {
        return extractClaim(token, claims -> UUID.fromString(claims.get("jti", String.class)));
    }

    public Set<String> extractRoles(String token) {
        List<?> rawRoles = extractClaim(token, claims -> claims.get("roles", List.class));
        return rawRoles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    public String generateAccessToken(
            UserDetails userDetails
    ) {
        return generateToken(new HashMap<>(), userDetails, TokenType.ACCESS, accessExpirationMs);
    }

    public String generateRefreshToken(
            UserDetails userDetails
    ) {
        return generateToken(new HashMap<>(), userDetails, TokenType.REFRESH, refreshExpirationMs);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            TokenType tokenType,
            long expirationMs
    ) {
        extraClaims.put("type", tokenType.name());

        long now = System.currentTimeMillis();
        if (tokenType == TokenType.REFRESH) {
            extraClaims.put("jti", UUID.randomUUID()
                    .toString());
        }
        if (tokenType == TokenType.ACCESS) {
            extraClaims.put("roles", userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet()));
        }
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSingInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractClaim(token, Claims::getSubject);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getSingInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getAccessTokenExpiry() {
        return accessExpirationMs;
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        try {
            String tokenType = extractTokenType(refreshToken);
            Optional<RevokedToken> revokedToken = revokedTokenService.findByJti(extractJti(refreshToken));
            if (!TokenType.REFRESH.name()
                    .equals(tokenType)) {
                log.warn("Invalid token: Incorrect token type");
                return false;
            }
            if (isTokenExpired(refreshToken)) {
                log.warn("The token has expired");
                return false;
            }
            if (revokedToken.isPresent()) {
                log.warn("The token has been revoked: jti={}", revokedToken.get()
                        .getJti());
                return false;
            }
            return true;


        } catch (Exception e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAccessTokenValid(String accessToken, UserDetails userDetails) {
        try {
            String tokenType = extractTokenType(accessToken);
            return TokenType.ACCESS.name()
                    .equals(tokenType) && isTokenValid(accessToken, userDetails);
        } catch (Exception e) {
            log.warn("Access token không hợp lệ: {}", e.getMessage());
            return false;
        }
    }


}

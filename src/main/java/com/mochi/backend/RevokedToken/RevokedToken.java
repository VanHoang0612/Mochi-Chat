package com.mochi.backend.RevokedToken;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {
    @Id
    UUID jti;
    LocalDateTime expiresAt;
    LocalDateTime revokedAt;


}

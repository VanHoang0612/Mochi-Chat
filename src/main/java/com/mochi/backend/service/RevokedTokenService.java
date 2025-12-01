package com.mochi.backend.service;

import com.mochi.backend.model.RevokedToken;
import com.mochi.backend.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RevokedTokenService {
    RevokedTokenRepository revokedTokenRepository;

    public Optional<RevokedToken> findByJti(UUID jti) {
        return revokedTokenRepository.findByJti(jti);
    }

    public boolean existsByJti(UUID jti) {
        return revokedTokenRepository.existsByJti(jti);
    }

    public void save(RevokedToken revokedToken) {
        revokedTokenRepository.save(revokedToken);
    }
}

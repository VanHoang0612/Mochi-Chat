package com.mochi.backend.service;

import com.mochi.backend.enums.ErrorCode;
import com.mochi.backend.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
    private final StringRedisTemplate redisTemplate;
    private final Duration ttl = Duration.ofMinutes(5);
    private final RedisService redisService;

    public String generateCode(String email) {
        String code = String.valueOf(new Random().nextInt(999999));
        redisService.saveValue(email, code);
        return code;
    }

    public boolean verifyOtp(String email, String code) {
        String storedCode = redisService.getValue(email);
        if (storedCode == null) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (code.equals(storedCode)) {
            redisService.deleteValue(email);
            return true;
        }
        throw new AppException(ErrorCode.VERIFICATION_CODE_INVALID);
    }
}

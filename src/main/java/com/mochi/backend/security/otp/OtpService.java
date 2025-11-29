package com.mochi.backend.security.otp;

import com.mochi.backend.exception.AppException;
import com.mochi.backend.shared.enums.ErrorCode;
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

    public String generateCode(String email) {
        String code = String.valueOf(new Random().nextInt(999999));
        redisTemplate.opsForValue()
                .set(email, code, ttl);
        return code;
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue()
                .get(email);
        if (storedCode == null) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        return code.equals(storedCode);
    }

    public void clearCode(String email) {
        redisTemplate.delete(email);
    }
}

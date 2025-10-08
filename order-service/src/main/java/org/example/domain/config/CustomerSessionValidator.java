package org.example.domain.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerSessionValidator {

    private final RedisTemplate<String, String> redisTemplate;

    // customerKey 레디스 검증 (만료 3시간)
    public void validate(String customerKey) {
        String redisKey = "session:" + customerKey;
        Boolean exists = redisTemplate.hasKey(redisKey);

        if (!Boolean.TRUE.equals(exists)) {
            log.warn("유효하지 않은 customerKey: {}", customerKey);
            throw new IllegalArgumentException("세션이 만료되었거나 존재하지 않습니다. QR을 다시 스캔해주세요.");
        }
    }
}


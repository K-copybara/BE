package org.example.domain.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.customer.dto.response.CustomerSessionResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerSessionService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long SESSION_TTL_MINUTES = 180; // 3시간

    // customerKey 생성
    public CustomerSessionResponse createSession(Long storeId, Long tableId) {
        String customerKey = "cust-" + UUID.randomUUID();
        String redisKey = "session:" + customerKey;

        // Redis에 storeId + tableId 함께 저장
        String sessionData = storeId + ":" + tableId;
        redisTemplate.opsForValue().set(redisKey, sessionData, Duration.ofMinutes(SESSION_TTL_MINUTES));

        log.info("✅ 고객 세션 생성: storeId={}, tableId={}, customerKey={}", storeId, tableId, customerKey);

        return new CustomerSessionResponse(
                storeId,
                tableId,
                customerKey,
                LocalDateTime.now().plusMinutes(SESSION_TTL_MINUTES)
        );
    }

    public boolean validateCustomerKey(String customerKey) {
        String redisKey = "session:" + customerKey;
        Boolean exists = redisTemplate.hasKey(redisKey);
        return Boolean.TRUE.equals(exists);
    }
}

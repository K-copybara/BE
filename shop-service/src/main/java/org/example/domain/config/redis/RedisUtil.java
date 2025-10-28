package org.example.domain.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private static final String PREFIX = "shop:";

    private final RedisTemplate<String, String> redisTemplate;

    // 저장
    public void save(String key, String value) {
        log.info("Redis SAVE [{}{}]", PREFIX, key);
        redisTemplate.opsForValue().set(PREFIX + key, value);
    }

    // 조회 (refreshToken 비교용)
    public String getData(String key) {
        return redisTemplate.opsForValue().get(PREFIX + key);
    }

    // 삭제 (로그아웃 시 사용 가능)
    public void delete(String key) {
        redisTemplate.delete(PREFIX + key);
    }

    // 토큰 블랙리스트 저장 후 TTL로 자동 만료
    public void setBlackList(String token, String value, long expirationMillis) {
        redisTemplate.opsForValue().set(PREFIX + token, value, expirationMillis, TimeUnit.MILLISECONDS);
    }

    // 블랙리스트 확인
    public boolean isBlackList(String token) {
        return redisTemplate.hasKey(PREFIX + token);
    }
}

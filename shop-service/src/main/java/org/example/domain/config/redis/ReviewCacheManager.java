package org.example.domain.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewCacheManager {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "review:";

    private String countKey(Long menuId) {
        return PREFIX + "menu:" + menuId + ":count";
    }

    private String scoreKey(Long menuId) {
        return PREFIX + "menu:" + menuId + ":avg";
    }

    public void updateStats(Long menuId, Long score) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        long count = 1L;
        double avg = score;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(countKey(menuId)))) {
            count = Long.parseLong(ops.get(countKey(menuId))) + 1;
            double oldAvg = Double.parseDouble(ops.get(scoreKey(menuId)));
            avg = ((oldAvg * (count - 1)) + score) / count;
        }

        ops.set(countKey(menuId), String.valueOf(count));
        ops.set(scoreKey(menuId), String.format("%.2f", avg));

        log.info("✅ Redis 리뷰 통계 갱신: menuId={}, count={}, avg={}", menuId, count, avg);
    }

    public void expireAtMidnight(Long menuId) {
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();
        long seconds = Duration.between(LocalDateTime.now(), midnight).getSeconds();
        redisTemplate.expire(countKey(menuId), seconds, TimeUnit.SECONDS);
        redisTemplate.expire(scoreKey(menuId), seconds, TimeUnit.SECONDS);
    }
}


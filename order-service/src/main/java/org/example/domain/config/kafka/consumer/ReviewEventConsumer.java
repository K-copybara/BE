package org.example.domain.config.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.config.kafka.dto.ReviewEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventConsumer {

    private final RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = "review.create", groupId = "order-review-cache")
    public void consume(ReviewEvent event) {
        log.info("✅ 리뷰 생성 이벤트 수신: {}", event);
        String key = "reviewed:" + event.getOrderId();
        redisTemplate.opsForValue().set(key, "true");
    }
}

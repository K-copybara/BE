package org.example.domain.config.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.config.kafka.dto.ReviewEvent;
import org.example.domain.config.redis.ReviewCacheManager;
import org.example.domain.entity.Menu;
import org.example.domain.entity.Rating;
import org.example.domain.menu.repository.MenuRepository;
import org.example.domain.rating.repository.RatingRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewConsumer {

    private final RatingRepository ratingRepository;
    private final MenuRepository menuRepository;
    private final ReviewCacheManager reviewCacheManager;

    @KafkaListener(topics = "review.create", groupId = "shop-review-group")
    public void consumeReview(ReviewEvent event) {
        log.info("📥 리뷰 수신: {}", event);

        Menu menu = menuRepository.findById(event.getMenuId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴를 찾을 수 없습니다."));

        // DB 저장
        Rating rating = Rating.builder()
                .menu(menu)
                .score(event.getScore())
                .build();
        ratingRepository.save(rating);

        // Redis 캐시 갱신
        reviewCacheManager.updateStats(event.getMenuId(), event.getScore());
        reviewCacheManager.expireAtMidnight(event.getMenuId());

        log.info("✅ 리뷰 저장 및 Redis 반영 완료 [menuId={}, score={}]", event.getMenuId(), event.getScore());
    }
}

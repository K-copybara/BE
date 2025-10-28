package org.example.domain.config.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.stats.service.CompletableFutureService;
import org.example.dto.Top3Menu.Top3MenuResponseEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Top3MenuResponseConsumer {

    private final CompletableFutureService futureService;

    @KafkaListener(topics = "top3-menu.response", groupId = "order-service-group")
    public void consume(Top3MenuResponseEvent event) {
        log.info("📥 Kafka 응답 수신 - 메뉴 정보: {}", event);
        // CompletableFutureService에 응답 전달
        futureService.completeResponse(event.getStoreId(), event);
    }
}
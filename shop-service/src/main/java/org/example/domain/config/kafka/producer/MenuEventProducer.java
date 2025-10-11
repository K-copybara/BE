package org.example.domain.config.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMenuEvent(Long storeId, Long menuId, String eventType) {
        Map<String, Object> message = Map.of(
                "storeId", storeId,
                "menuId", menuId,
                "eventType", eventType,   // CREATED, UPDATED, DELETED, STATUS_CHANGED
                "timestamp", LocalDateTime.now().toString()
        );

        kafkaTemplate.send("menu-updated", message);
        log.info("📤 Kafka 전송: menu-updated -> {}", message);
    }
}


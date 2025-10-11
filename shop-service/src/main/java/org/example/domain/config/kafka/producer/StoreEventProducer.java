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
public class StoreEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendStoreUpdatedEvent(Long storeId, String eventType) {
        Map<String, Object> message = Map.of(
                "storeId", storeId,
                "eventType", eventType,   // CREATED, UPDATED, DELETED
                "timestamp", LocalDateTime.now().toString()
        );

        kafkaTemplate.send("store-updated", message);
        log.info("📤 Kafka 전송: store-updated -> {}", message);
    }
}

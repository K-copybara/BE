package org.example.domain.config.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.Top3Menu.Top3MenuRequestEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTop3Producer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTop3MenuRequest(Long storeId, List<Long> menuIds) {
        Top3MenuRequestEvent event = new Top3MenuRequestEvent(storeId, menuIds);
        kafkaTemplate.send("top3-menu.request", event);
        log.info("📤 Kafka 발행 완료: {}", event);
    }
}

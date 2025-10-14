package org.example.domain.config.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.entity.Menu;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMenuEvent(Long storeId, Long menuId, String eventType, Menu menu) {
        // "우유,달걀" → ["우유", "달걀"]
        List<String> allergens = null;
        if (menu.getAllergy() != null && !menu.getAllergy().isBlank()) {
            allergens = Arrays.stream(menu.getAllergy().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        Map<String, Object> menuInfo = Map.of(
                "menu_name", menu.getMenuName(),
                "price", menu.getMenuPrice(),
                "description", menu.getMenuInfo(),   // 메뉴 설명
                "spiciness", menu.getSpicy(),        // 매운 정도
                "allergens", allergens != null ? allergens : List.of(),
                "extra_info", menu.getExtraInfo()
        );

        Map<String, Object> message = Map.of(
                "storeId", storeId,
                "menuId", menuId,
                "eventType", eventType,              // CREATED | UPDATED | DELETED
                "timestamp", LocalDateTime.now().toString(),
                "menu", menuInfo
        );

        kafkaTemplate.send("menu-updated", message);
        log.info("📤 Kafka 전송 완료 → Topic: menu-updated, Payload: {}", message);
    }
}


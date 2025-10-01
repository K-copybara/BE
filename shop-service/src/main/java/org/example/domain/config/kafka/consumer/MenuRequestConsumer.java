package org.example.domain.config.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.menu.repository.MenuRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuRequestConsumer {

    private final MenuRepository menuRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "menu-request", groupId = "shop-service")
    public void consumeMenuRequest(Map<String, Object> message) {
        log.info("consumeMenuRequest received: {}", message);

        String requestId = (String) message.get("requestId");

        // menuIds -> 항상 Long으로 변환
        List<Long> menuIds = ((List<?>) message.get("menuIds")).stream()
                .map(id -> ((Number) id).longValue())
                .toList();

        var menus = menuRepository.findAllById(menuIds).stream()
                .map(menu -> Map.of(
                        "menuId", menu.getId(),
                        "menuName", menu.getMenuName(),
                        "menuPrice", menu.getMenuPrice()
                ))
                .toList();

        Map<String, Object> response = Map.of(
                "requestId", requestId,
                "menus", menus
        );

        kafkaTemplate.send("menu-response", requestId, response);
    }
}
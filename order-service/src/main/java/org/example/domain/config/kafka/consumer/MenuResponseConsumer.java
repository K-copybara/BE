package org.example.domain.config.kafka.consumer;

import org.example.domain.config.kafka.dto.MenuDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MenuResponseConsumer {

    private final Map<String, CompletableFuture<List<MenuDto>>> pendingRequests = new ConcurrentHashMap<>();

    @KafkaListener(topics = "menu-response", groupId = "order-service")
    public void consumeMenuResponse(Map<String, Object> message) {
        String requestId = (String) message.get("requestId");
        List<Map<String, Object>> menus = (List<Map<String, Object>>) message.get("menus");

        List<MenuDto> menuDtos = menus.stream()
                .map(m -> new MenuDto(
                        ((Number) m.get("menuId")).longValue(),
                        (String) m.get("menuName"),
                        ((Number) m.get("menuPrice")).intValue(),
                        (String) m.get("menuCategory")
                ))
                .toList();

        CompletableFuture<List<MenuDto>> future = pendingRequests.remove(requestId);
        if (future != null) {
            future.complete(menuDtos);
        }
    }

    public CompletableFuture<List<MenuDto>> registerRequest(String requestId) {
        CompletableFuture<List<MenuDto>> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        return future;
    }

}


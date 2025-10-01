package org.example.domain.config.kafka.consumer;

import org.example.domain.config.kafka.dto.CartMenuDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartMenuResponseConsumer {

    private final Map<String, CompletableFuture<List<CartMenuDto>>> pendingRequests = new ConcurrentHashMap<>();

    @KafkaListener(topics = "cart-menu-response", groupId = "order-service")
    public void consumeMenuResponse(Map<String, Object> message) {
        String requestId = (String) message.get("requestId");
        List<Map<String, Object>> menus = (List<Map<String, Object>>) message.get("menus");

        List<CartMenuDto> menuDtos = menus.stream()
                .map(m -> CartMenuDto.builder()
                        .menuId(((Number) m.get("menuId")).longValue())
                        .menuName((String) m.get("menuName"))
                        .menuPrice(((Number) m.get("menuPrice")).longValue())
                        .menuCategory((String) m.get("menuCategory"))
                        .menuPicture((String) m.get("menuPicture"))
                        .build()
                )
                .toList();

        CompletableFuture<List<CartMenuDto>> future = pendingRequests.remove(requestId);
        if (future != null) {
            future.complete(menuDtos);
        }
    }

    public CompletableFuture<List<CartMenuDto>> registerRequest(String requestId) {
        CompletableFuture<List<CartMenuDto>> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        return future;
    }
}


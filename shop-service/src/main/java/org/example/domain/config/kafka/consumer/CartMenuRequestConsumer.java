package org.example.domain.config.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.menu.repository.MenuRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartMenuRequestConsumer {

    private final MenuRepository menuRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "cart-menu-request", groupId = "shop-service")
    public void consumeMenuRequest(Map<String, Object> message) {
        log.info("consumeCartMenuRequest received: {}", message);

        try {
            String requestId = (String) message.get("requestId");

            // menuIds 안전하게 변환
            List<Long> menuIds = ((List<?>) message.get("menuIds")).stream()
                    .map(id -> ((Number) id).longValue())
                    .toList();

            // 메뉴 조회 + null-safe DTO 변환
            var menus = menuRepository.findAllByIdWithCategory(menuIds).stream()
                    .map(menu -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("menuId", menu.getId());
                        dto.put("menuName", menu.getMenuName());
                        dto.put("menuPrice", menu.getMenuPrice());
                        dto.put("menuCategory", menu.getCategory().getCategoryName()); // 이제 안전
                        dto.put("menuPicture", menu.getMenuPicture() != null ? menu.getMenuPicture() : "");
                        return dto;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("menus", menus);

            kafkaTemplate.send("cart-menu-response", requestId, response);
            log.info("✅ cart-menu-response sent: {}", response);

        } catch (Exception e) {
            log.error("❌ CartMenuRequestConsumer 처리 중 에러", e);
            throw e; // rethrow 해서 retry는 하되 원인 로그를 확실히 찍음
        }
    }
}


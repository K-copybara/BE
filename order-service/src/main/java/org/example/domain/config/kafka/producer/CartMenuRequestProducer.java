package org.example.domain.config.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartMenuRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMenuRequest(String requestId, List<Long> menuIds) {
        Map<String, Object> message = Map.of(
                "requestId", requestId,
                "menuIds", menuIds
        );
        kafkaTemplate.send("cart-menu-request", requestId, message);
    }
}


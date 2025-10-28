package org.example.domain.config.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.OrderPaidEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderPaid(OrderPaidEvent event) {
        kafkaTemplate.send("order-paid", event);
        log.info("✅ Kafka 발행 완료: {}", event.getOrderId());
    }
}

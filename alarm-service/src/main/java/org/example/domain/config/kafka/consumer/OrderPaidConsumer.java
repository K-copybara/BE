package org.example.domain.config.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.config.SSE.SseEmitterService;
import org.example.dto.OrderPaidEvent;
import org.example.dto.Response;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPaidConsumer {

    private final SseEmitterService sseEmitterService;

    @KafkaListener(topics = "order-paid", groupId = "notification-group")
    public void consume(OrderPaidEvent event) {
        log.info("💬 Kafka 이벤트 수신: {}", event.getOrderId());

        // 글로벌 Response 래핑
        Response<OrderPaidEvent> response = Response.success("주문 알림 수신", event);

        // SSE로 전송
        sseEmitterService.sendToStore(event.getStoreId(), response);
    }
}

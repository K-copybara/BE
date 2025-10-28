package org.example.domain.config.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.config.SSE.RequestSseEmitterService;
import org.example.dto.RequestCreatedEvent;
import org.example.dto.Response;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestNotificationConsumer {

    private final RequestSseEmitterService requestSseEmitterService;

    @KafkaListener(topics = "request-created-notification", groupId = "notification-service")
    public void consume(RequestCreatedEvent event) {
        log.info("📩 Kafka 요청 알림 수신: {}", event.getRequestNote());

        Response<RequestCreatedEvent> response = Response.success("요청 알림 수신", event);

        // 사장님 storeId 기준으로 SSE 전송
        requestSseEmitterService.sendToStore(event.getStoreId(), response);
    }
}

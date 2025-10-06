package org.example.domain.config.SSE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RequestSseEmitterService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RequestSseEmitterService() {
        this.objectMapper = new ObjectMapper();
        // LocalDateTime을 ISO-8601 문자열로 직렬화
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    public SseEmitter subscribe(Long storeId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        emitters.put(storeId, emitter);

        emitter.onCompletion(() -> emitters.remove(storeId));
        emitter.onTimeout(() -> emitters.remove(storeId));

        log.info("🟢 요청 SSE 구독 시작: {}", storeId);
        return emitter;
    }

    public void sendToStore(Long storeId, Object data) {
        SseEmitter emitter = emitters.get(storeId);
        if (emitter != null) {
            try {
                // ✅ 전송 전 JSON 직렬화 로그 출력
                String json = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(data);

                log.info("📤 실제 전송 데이터 (storeId={}):\n{}", storeId, json);

                emitter.send(SseEmitter.event()
                        .name("request-created")
                        .data(data));

                log.info("📨 요청 알림 전송 완료: storeId={}", storeId);
            } catch (IOException e) {
                emitters.remove(storeId);
                log.error("❌ 요청 SSE 전송 실패", e);
            }
        }
    }
}

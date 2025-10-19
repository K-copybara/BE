package org.example.domain.config.SSE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class SseEmitterService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 하트비트용 스케줄러

    public SseEmitterService() {
        this.objectMapper = new ObjectMapper();
        // LocalDateTime을 ISO-8601 문자열로 직렬화
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public SseEmitter subscribe(Long storeId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간 유지
        emitters.put(storeId, emitter);

        emitter.onCompletion(() -> emitters.remove(storeId));
        emitter.onTimeout(() -> emitters.remove(storeId));

        log.info("🟢 주문 SSE 구독 시작: {}", storeId);

        // 연결 직후 초기 신호 전송 (idle timeout 방지)
        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data("connected"));
            log.info("📡 주문 초기 이벤트 전송 완료: storeId={}", storeId);
        } catch (IOException e) {
            log.error("❌ 주문 초기 이벤트 전송 실패: {}", e.getMessage());
        }

        // 하트비트 전송 (25초 간격)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                SseEmitter e = emitters.get(storeId);
                if (e != null) {
                    e.send(SseEmitter.event().comment("keepalive"));
                    log.debug("📍주문 keepalive: storeId={}", storeId);
                }
            } catch (IOException ex) {
                emitters.remove(storeId);
                log.warn("❌ 주문 하트비트 실패, 연결 종료: storeId={}", storeId);
            }
        }, 25, 25, TimeUnit.SECONDS);

        return emitter;
    }

    public void sendToStore(Long storeId, Object data) {
        SseEmitter emitter = emitters.get(storeId);
        if (emitter != null) {
            try {
//                // JSON → UTF-8 바이트 배열로 직접 변환
//                String json = objectMapper.writeValueAsString(data);
//                log.info("📤 전송 JSON: {}", json);
//
//                emitter.send(SseEmitter.event()
//                        .name("order-paid")
//                        .data(json,MediaType.valueOf("text/event-stream; charset=UTF-8")));

                // 전송 전 JSON 직렬화 로그 출력
                String json = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(data);

                log.info("📤 주문 실제 전송 데이터 (storeId={}):\n{}", storeId, json);

                emitter.send(SseEmitter.event()
                        .name("order-paid")
                        .data(data));

                log.info("📨 주문 알림 전송 완료: storeId={}", storeId);
            } catch (IOException e) {
                emitters.remove(storeId);
                log.error("❌ 주문 SSE 전송 실패", e);
            }
        }
    }
}


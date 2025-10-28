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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RequestSseEmitterService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    // 하트비트를 주기적으로 보내기 위한 스케줄러 (백그라운드 타이머 쓰레드)
    // 서버가 일정 주기마다 클라이언트에게 ping 신호
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 1개 스레드를 가진 타이머용 스레드 풀

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

        // 연결 직후 한 줄이라도 전송 (idle timeout 방지)
        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data("connected"));
            log.info("📡 초기 이벤트 전송 완료: storeId={}", storeId);
        } catch (IOException e) {
            log.error("❌ 초기 이벤트 전송 실패: {}", e.getMessage());
        }

        // 하트비트 전송 (25초 간격)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                SseEmitter e = emitters.get(storeId);
                if (e != null) {
                    e.send(SseEmitter.event().comment("keepalive"));
                    log.debug("📍 keepalive: storeId={}", storeId);
                }
            } catch (IOException ex) {
                emitters.remove(storeId);
                log.warn("❌ 하트비트 실패, 연결 종료: storeId={}", storeId);
            }
        }, 25, 25, TimeUnit.SECONDS);


        return emitter;
    }

    public void sendToStore(Long storeId, Object data) {
        SseEmitter emitter = emitters.get(storeId);
        if (emitter != null) {
            try {
                // 전송 전 JSON 직렬화 로그 출력
                String json = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(data);

                log.info("📤 실제 전송 데이터 (storeId={}):\n{}", storeId, json);

                emitter.send(SseEmitter.event()
                        .name("request-created-notification")
                        .data(data));

                log.info("📨 요청 알림 전송 완료: storeId={}", storeId);
            } catch (IOException e) {
                emitters.remove(storeId);
                log.error("❌ 요청 SSE 전송 실패", e);
            }
        }
    }
}

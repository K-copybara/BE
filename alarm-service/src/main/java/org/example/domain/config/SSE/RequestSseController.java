package org.example.domain.config.SSE;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.dto.RequestCreatedEvent;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/notify/request")
public class RequestSseController {

    private final RequestSseEmitterService requestSseEmitterService;

    // 구독 (사장님 화면이 이 API를 구독함)
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter subscribe(
            @RequestParam Long storeId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // SSE 전용 CORS 헤더 추가
        String origin = request.getHeader("Origin");

        // 개발용 & 배포용 도메인 모두 허용
        String allowedOrigin = "https://tabletalk-copybara.netlify.app";
        if ("http://localhost:3000".equals(origin)) {
            allowedOrigin = "http://localhost:3000";
        } else if ("http://localhost:5173".equals(origin)) {
            allowedOrigin = "http://localhost:5173";
        }

        response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        response.setHeader("Access-Control-Allow-Credentials", "true");

        response.setHeader("Cache-Control", "no-cache");          // 캐싱 방지
        response.setHeader("X-Accel-Buffering", "no");            // Nginx 버퍼링 방지
        response.setHeader("Content-Type", "text/event-stream;charset=UTF-8"); // 명시적 설정

        return requestSseEmitterService.subscribe(storeId);
    }

    // 테스트용 이벤트 발행
    @PostMapping("/test")
    public ResponseEntity<String> sendTestEvent(@RequestParam Long storeId) {
        RequestCreatedEvent event = RequestCreatedEvent.builder()
                .requestId(501L)
                .tableId(5L)
                .requestedAt(LocalDateTime.of(2025, 9, 14, 15, 30))
                .requestNote("에어컨 너무 추워요. 온도 내려주세요")
                .items(List.of(
                        new RequestCreatedEvent.RequestItemDto("물티슈", 1L),
                        new RequestCreatedEvent.RequestItemDto("휴지", 2L)
                ))
                .build();

        Response<RequestCreatedEvent> response = Response.success("요청 알림 수신", event);

        requestSseEmitterService.sendToStore(storeId, response);
        return ResponseEntity.ok("테스트 요청 알림 전송 완료 (storeId=" + storeId + ")");
    }
}

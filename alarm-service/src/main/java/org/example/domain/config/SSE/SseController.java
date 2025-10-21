package org.example.domain.config.SSE;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.dto.OrderPaidEvent;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/notify")
public class SseController {

    private final SseEmitterService sseEmitterService;

    // SSE 구독
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter subscribe(
            @RequestParam Long storeId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // SSE 전용 CORS 허용
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

        return sseEmitterService.subscribe(storeId);
    }

    // 테스트용 이벤트 발행
    @PostMapping("/test")
    public ResponseEntity<String> sendTestEvent(@RequestParam Long storeId) {
        // 샘플 주문 이벤트 생성
        OrderPaidEvent event = OrderPaidEvent.builder()
                .orderId("TEST-ORDER-" + System.currentTimeMillis())
                .storeId(storeId)
                .tableId(5L)
                .orderedAt(LocalDateTime.now())
                .requestNote("얼음 조금만 넣어주세요")
                .items(List.of(
                        new OrderPaidEvent.OrderItemDto(1L, "아메리카노", 2),
                        new OrderPaidEvent.OrderItemDto(2L, "카페라떼", 1)
                ))
                .build();

        // 공통 Response 포맷으로 감싸기
        Response<OrderPaidEvent> response = Response.success("주문 알림 수신", event);

        // SSE로 전송
        sseEmitterService.sendToStore(storeId, response);

        // 호출자(Postman)에게 반환
        return ResponseEntity.ok("테스트 알림 전송 완료 (storeId=" + storeId + ")");
    }

    
}

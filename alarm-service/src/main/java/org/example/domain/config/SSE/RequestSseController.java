package org.example.domain.config.SSE;

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
    public SseEmitter subscribe(@RequestParam Long storeId) {
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

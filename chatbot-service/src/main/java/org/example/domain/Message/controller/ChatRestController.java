package org.example.domain.Message.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.Message.dto.AiResponse;
import org.example.domain.Message.dto.ChatRequestDto;
import org.example.domain.Message.repository.ChatSessionRepository;
import org.example.domain.Message.repository.MessageRepository;
import org.example.domain.Message.service.ChatService;
import org.example.domain.entity.ChatSession;
import org.example.domain.entity.Message;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/chat")
public class ChatRestController {

    private final ChatService chatService;
    private final ChatSessionRepository chatSessionRepository;
    private final MessageRepository messageRepository;

    // 메세지 전송 (rest)
    @PostMapping("/session/{sessionId}/message")
    public ResponseEntity<Response<?>> sendMessage(
            @PathVariable Long sessionId,
            @RequestBody ChatRequestDto req) {

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        // ChatService에서 AI 호출 + DB 저장 + 응답 Map 반환
        Map<String, Object> aiMessage = chatService.askAi(
                session.getCustomerKey(),
                req.getMessage(),
                session
        );

        // 응답 데이터 (BOT 메시지 기준)
        return ResponseEntity.ok(Response.success("메시지 전송 성공", aiMessage));
    }

    // 메세지 목록 조회 (이전 내역)
    @GetMapping("/session/{sessionId}/messages")
    public ResponseEntity<Response<?>> getMessages(@PathVariable Long sessionId) {
        List<Map<String, Object>> messages = chatService.getMessages(sessionId);
        return ResponseEntity.ok(Response.success("챗봇 메시지 조회 성공", messages));
    }
}

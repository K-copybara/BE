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

        AiResponse aiResponse = chatService.askAi(session.getCustomerKey(), req.getMessage(), session);

        Map<String, Object> data = Map.of(
                "role", "CUSTOMER",
                "content", req.getMessage(),
                "sentAt", aiResponse.getSentAt()
        );

        return ResponseEntity.ok(Response.success("메시지 전송 성공", data));
    }

    // 메세지 목록 조회 (이전 내역)
    @GetMapping("/session/{sessionId}/messages")
    public ResponseEntity<Response<?>> getMessages(@PathVariable Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        var messages = messageRepository.findByChatSessionOrderBySentAtAsc(session)
                .stream()
                .map(m -> Map.of(
                        "messageId", m.getId(),
                        "role", m.getRole(),
                        "content", m.getMessage(),
                        "sentAt", m.getSentAt()
                ))
                .toList();

        return ResponseEntity.ok(Response.success("챗봇 메시지 조회 성공", messages));
    }
}

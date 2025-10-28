package org.example.domain.Message.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.Message.repository.ChatSessionRepository;
import org.example.domain.Message.service.ChatService;
import org.example.domain.entity.ChatSession;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/chat")
public class ChatSessionController {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatService chatService;

    // 처음 대화 시작 전 채팅방 만들기
    @PostMapping("/session")
    public ResponseEntity<Response<?>> createSession(@RequestBody Map<String, Object> req) {
        Long storeId = Long.valueOf(req.get("storeId").toString());
        Long tableId = Long.valueOf(req.get("tableId").toString());
        String customerKey = req.get("customerKey").toString();

        ChatSession session = chatService.createSession(storeId, tableId, customerKey);

        String message = (session.getId() != null)
                ? "챗봇 세션 생성 성공"
                : "이미 존재하는 세션입니다.";

        Map<String, Object> data = Map.of("sessionId", session.getId());
        return ResponseEntity.ok(Response.success(message, data));
    }


}

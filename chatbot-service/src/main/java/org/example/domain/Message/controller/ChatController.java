package org.example.domain.Message.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Message.dto.AiResponse;
import org.example.domain.Message.dto.ChatMessage;
import org.example.domain.Message.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 클라이언트에서 /app/chat.send 로 메시지 전송 시 작동
    // 웹소켓으로 메세지 전송할 때마다 작동
    @MessageMapping("/chat.send")
    public void handleMessage(ChatMessage chatMessage) {
        log.info("📨 [{}] 사용자 메시지 수신: {}", chatMessage.getCustomerKey(), chatMessage.getContent());

        // AI 응답 + 저장 + messageId 포함된 Map 반환
        var responsePayload = chatService.askAi(chatMessage);

        // AI 응답을 해당 사용자 토픽으로 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatMessage.getCustomerKey(),
                responsePayload
        );
    }
}

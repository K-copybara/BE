package org.example.domain.Message.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Message.dto.AiResponse;
import org.example.domain.Message.dto.ChatMessage;
import org.example.domain.Message.repository.ChatSessionRepository;
import org.example.domain.Message.repository.MessageRepository;
import org.example.domain.entity.ChatSession;
import org.example.domain.entity.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final AiClient aiClient;
    private final ChatSessionRepository chatSessionRepository;
    private final MessageRepository messageRepository;

    // 채팅 세션 생성 (중복 체크 포함)
    @Transactional
    public ChatSession createSession(Long storeId, Long tableId, String customerKey) {

        // 기존 세션 존재 여부 확인
        return chatSessionRepository.findByCustomerKey(customerKey)
                .orElseGet(() -> {
                    // 없으면 새 세션 생성
                    ChatSession newSession = ChatSession.builder()
                            .storeId(storeId)
                            .tableId(tableId)
                            .customerKey(customerKey)
                            .build();
                    return chatSessionRepository.save(newSession);
                });
    }

    // STOMP 용
    @Transactional
    public Map<String, Object> askAi(ChatMessage chatMessage) {
        ChatSession session = chatSessionRepository.findByCustomerKey(chatMessage.getCustomerKey())
                .orElseThrow(() -> new IllegalArgumentException("세션이 존재하지 않습니다."));

        return askAi(session.getCustomerKey(), chatMessage.getContent(), session);
    }

    // REST 용 (공통 내부 로직)
    @Transactional
    public Map<String, Object> askAi(String customerKey, String content, ChatSession session) {

        // 사용자 메시지 저장
        Message customerMsg = Message.builder()
                .chatSession(session)
                .role("CUSTOMER")
                .message(content)
                .build();
        messageRepository.save(customerMsg);

        // FastAPI 호출 (AI 응답 받기)
        AiResponse aiResponse = aiClient.ask(
                session.getCustomerKey(),
                content,
                session.getStoreId(),
                session.getTableId()
        );

        // AI 응답 메시지 저장
        Message botMsg = Message.builder()
                .chatSession(session)
                .role("BOT")
                .message(aiResponse.getAnswer())
                .build();
        messageRepository.save(botMsg);

        // 반환할 데이터 구성 (명세서에 맞춤)
        return Map.of(
                "messageId", botMsg.getId(),
                "role", botMsg.getRole(),
                "content", botMsg.getMessage(),
                "sentAt", botMsg.getSentAt()
        );
    }

    // 채팅 메세지 목록 조회 (이전 내역)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMessages(Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

        return messageRepository.findByChatSessionOrderBySentAtAsc(session)
                .stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("messageId", m.getId());
                    map.put("role", m.getRole());
                    map.put("content", m.getMessage());
                    map.put("sentAt", m.getSentAt());
                    return map;
                })
                .toList();

    }

}


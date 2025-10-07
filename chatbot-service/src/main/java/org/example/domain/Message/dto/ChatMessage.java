package org.example.domain.Message.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private String customerKey;  // 세션 식별자
    private String role;         // "CUSTOMER" /"BOT"
    private String content;      // 메시지 내용
}

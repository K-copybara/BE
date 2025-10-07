package org.example.domain.Message.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiResponse {
    private String answer;
    private String sentAt; // FastAPI에서 보낸 시간 문자열 수신용

    // 단일 인자 생성자 추가
    public AiResponse(String answer) {
        this.answer = answer;
        this.sentAt =  LocalDateTime.now().toString();
    }
}


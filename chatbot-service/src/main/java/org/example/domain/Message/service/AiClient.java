package org.example.domain.Message.service;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.Message.dto.AiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
public class AiClient {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8000") // FastAPI 서버 주소
            .build();

    public AiResponse ask(Map<String, Object> payload) {
        try {
            return webClient.post()
                    .uri("/rag/ask")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(AiResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("❌ AI 서비스 호출 실패: {}", e.getMessage());
            return new AiResponse("AI 서버가 응답하지 않습니다.");
        }
    }
}
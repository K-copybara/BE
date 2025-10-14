package org.example.domain.Message.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Message.dto.AiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
public class AiClient {

    @Value("${ai.base-url}")
    private String baseUrl;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

//    private final WebClient webClient = WebClient.builder()
//            .baseUrl("http://localhost:8000") // FastAPI 서버 주소
//            .build();


    public AiResponse ask(String customerKey, String userInput, Long storeId, Long tableId) {
        Map<String, Object> body = Map.of(
                "customer_key", customerKey,
                "user_input", userInput,
                "store_id", storeId,
                "table_id", tableId
        );

        return webClient.post()
                .uri("/chat/response")   // FastAPI 엔드포인트
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AiResponse.class)
                .block();
    }

}
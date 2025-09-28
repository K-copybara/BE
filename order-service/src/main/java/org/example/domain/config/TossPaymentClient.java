//package org.example.domain.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import lombok.RequiredArgsConstructor;
//import org.example.domain.pay.dto.request.ConfirmPaymentRequest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpResponse;
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//@RequiredArgsConstructor
//public class TossPaymentClient {
//
//    @Value("${payment.secret.key}")
//    private String secretKey;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public ResponseEntity<String> requestConfirm(ConfirmPaymentRequest confirmPaymentRequest) {
//        String url = "https://api.tosspayments.com/v1/payments/confirm";
//
//        // Authorization 헤더 (Basic 인증)
//        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Basic " + encodedAuth);
//
//        // 요청 바디
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("orderId", confirmPaymentRequest.orderId());
//        requestBody.put("amount", confirmPaymentRequest.amount());
//        requestBody.put("paymentKey", confirmPaymentRequest.paymentKey());
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//
//        // Toss API 호출
//        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//    }
//
//
//    public ResponseEntity<Map> requestPaymentCancel(String paymentKey, String cancelReason) {
//        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
//
//        String encodedAuth = Base64.getEncoder()
//                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Basic " + encodedAuth);
//
//        Map<String, String> body = Map.of("cancelReason", cancelReason);
//
//        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
//
//        return restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
//    }
//}
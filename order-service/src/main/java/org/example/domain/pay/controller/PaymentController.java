//package org.example.domain.pay.controller;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import org.example.domain.config.TossPaymentClient;
//import org.example.domain.entity.Orders;
//import org.example.domain.entity.TossPayment;
//import org.example.domain.entity.TossPaymentMethod;
//import org.example.domain.entity.TossPaymentStatus;
//import org.example.domain.pay.dto.request.ConfirmPaymentRequest;
//import org.example.domain.pay.dto.request.SaveAmountRequest;
//import org.example.domain.pay.dto.response.PaymentErrorResponse;
//import org.example.domain.pay.repository.OrdersRepository;
//import org.example.domain.pay.service.PaymentService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import org.springframework.http.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Base64;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/payments")
//@RequiredArgsConstructor
//public class PaymentController {
//
//    @Value("${payment.secret.key}")
//    private String secretKey;
//
//    private final PaymentService paymentService;
//    private final OrdersRepository ordersRepository;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    // 세션에 orderId -> amount 저장
//    // 결제 승인 시 비교해서 금액 변조 여부 확인
//    @PostMapping("/saveAmount")
//    public ResponseEntity<?> tempSave(HttpSession session, @RequestBody SaveAmountRequest saveAmountRequest) {
//        session.setAttribute(saveAmountRequest.orderId(), saveAmountRequest.amount());
//        return ResponseEntity.ok("Payment temp save successful");
//    }
//
//    // 결제 금액 검증
//    @PostMapping("/verifyAmount")
//    public ResponseEntity<?> verifyAmount(HttpSession session, @RequestBody SaveAmountRequest saveAmountRequest) {
//
//        Integer savedAmount = (Integer) session.getAttribute(saveAmountRequest.orderId());
//
//        // 결제 전의 금액과 결제 후의 금액이 같은지 검증
//        if (savedAmount == null || !savedAmount.equals(saveAmountRequest.amount())) {
//            return ResponseEntity.badRequest()
//                    .body(PaymentErrorResponse.builder()
//                            .code(400)
//                            .message("결제 금액 정보가 유효하지 않습니다.")
//                            .build());
//        }
//
//        // 검증에 사용했던 세션은 삭제
//        session.removeAttribute(saveAmountRequest.orderId());
//
//        return ResponseEntity.ok("Payment is valid");
//    }
//
//    /**
//     * 결제 승인 (토스 → 우리 서버 → 토스)
//     */
//    @PostMapping("/confirm")
//    public ResponseEntity<?> confirmPayment(@RequestBody ConfirmPaymentRequest confirmPaymentRequest) {
//        try {
//            // ✅ Toss API 요청
//            ResponseEntity<String> response = requestConfirm(confirmPaymentRequest);
//
//            if (response.getStatusCode() == HttpStatus.OK) {
//                try {
//                    // 응답 Body 파싱
//                    JsonNode body = objectMapper.readTree(response.getBody());
//
//                    // 승인 시간
//                    LocalDateTime approvedAt = LocalDateTime.parse(
//                            body.get("approvedAt").asText(),
//                            DateTimeFormatter.ISO_OFFSET_DATE_TIME
//                    );
//
//                    // 결제 수단 / 상태
//                    TossPaymentMethod method = TossPaymentMethod.valueOf(body.get("method").asText().toUpperCase());
//                    TossPaymentStatus status = TossPaymentStatus.APPROVED;
//
//                    // 주문 조회
//                    Orders orders = ordersRepository.findById(Long.valueOf(confirmPaymentRequest.orderId()))
//                            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
//
//                    // DB 저장
//                    TossPayment saved = paymentService.savePayment(
//                            confirmPaymentRequest,
//                            orders,
//                            method,
//                            status,
//                            approvedAt
//                    );
//
//                    return ResponseEntity.ok(saved);
//
//                } catch (Exception e) {
//                    // DB 저장 실패 → 결제 취소
//                    requestPaymentCancel(confirmPaymentRequest.paymentKey(), "DB 저장 실패");
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .body(PaymentErrorResponse.builder()
//                                    .code(500)
//                                    .message("결제 저장 중 오류 발생. 결제를 취소했습니다.")
//                                    .build());
//                }
//            } else {
//                // Toss 결제 승인 실패
//                return ResponseEntity.status(response.getStatusCode())
//                        .body(PaymentErrorResponse.builder()
//                                .code(response.getStatusCode().value())
//                                .message("토스 결제 승인 실패")
//                                .build());
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(PaymentErrorResponse.builder()
//                            .code(500)
//                            .message("결제 승인 처리 중 예외 발생: " + e.getMessage())
//                            .build());
//        }
//    }
//
//    /**
//     * Toss API에 결제 승인 요청
//     */
//    private ResponseEntity<String> requestConfirm(ConfirmPaymentRequest confirmPaymentRequest) throws JsonProcessingException {
//        String url = "https://api.tosspayments.com/v1/payments/confirm";
//
//        // 요청 JSON
//        Map<String, Object> requestObj = Map.of(
//                "orderId", confirmPaymentRequest.orderId(),
//                "amount", confirmPaymentRequest.amount(),
//                "paymentKey", confirmPaymentRequest.paymentKey()
//        );
//
//        // Authorization 헤더 (Basic base64(secretKey:))
//        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Basic " + encodedAuth);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestObj, headers);
//
//        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//    }
//
//    /**
//     * Toss API에 결제 취소 요청
//     */
//    private void requestPaymentCancel(String paymentKey, String cancelReason) {
//        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
//
//        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Basic " + encodedAuth);
//
//        Map<String, String> body = Map.of("cancelReason", cancelReason);
//
//        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
//
//        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//    }
//
//
//
//
//}

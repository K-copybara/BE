package org.example.domain.pay.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.pay.dto.request.ConfirmPaymentRequest;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.example.domain.entity.Orders;
import org.example.domain.entity.TossPayment;
import org.example.domain.entity.TossPaymentMethod;
import org.example.domain.entity.TossPaymentStatus;
import org.example.domain.pay.dto.response.ChargeResponse;
import org.example.domain.pay.repository.OrdersRepository;
import org.example.domain.pay.repository.TossPaymentRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {

    @Value("${payment.secret.key}")
    private String API_SECRET_KEY;

    private final TossPaymentRepository tossPaymentRepository;
    private final OrdersRepository ordersRepository;

    @Transactional
    public ChargeResponse confirmPayment(ConfirmPaymentRequest request) throws IOException {
        // 1. Toss API 호출을 위한 JSON 구성
        JSONObject requestObj = new JSONObject();
        requestObj.put("orderId", request.orderId());
        requestObj.put("amount", request.amount());
        requestObj.put("paymentKey", request.paymentKey());

        JSONObject response = sendRequest(requestObj, API_SECRET_KEY,
                "https://api.tosspayments.com/v1/payments/confirm");

        if (response.containsKey("error")) {
            throw new RuntimeException("토스 결제 승인 실패: " + response.get("error"));
        }

        // 2. 승인된 결제 정보 파싱
        String orderId = (String) response.get("orderId");
        Long amount = Long.valueOf(response.get("totalAmount").toString());
        String paymentKey = (String) response.get("paymentKey");

        Orders orders = ordersRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        TossPayment tossPayment = TossPayment.builder()
                .paymentId(UUID.randomUUID())
                .orders(orders)
                .tossOrderId(orderId)
                .tossPaymentKey(paymentKey)
                .tossPaymentMethod((String) response.get("method")) // 실제 response에서 method 꺼내서 매핑
                .tossPaymentStatus(TossPaymentStatus.APPROVED)
                .requestedAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.now())
                .totalAmount(amount)
                .build();

        tossPaymentRepository.save(tossPayment);

        return new ChargeResponse(amount, "결제 성공");
    }

    private JSONObject sendRequest(JSONObject requestData, String secretKey, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(secretKey, urlString);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream responseStream = connection.getResponseCode() == 200
                ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            log.error("Error reading response", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Error reading response");
            return errorResponse;
        }
    }

    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " +
                Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }

//    private JSONObject parseRequestData(String jsonBody) {
//        try {
//            return (JSONObject) new JSONParser().parse(jsonBody);
//        } catch (ParseException e) {
//            log.error("JSON Parsing Error", e);
//            return new JSONObject(); // 실패 시 빈 JSON 반환
//        }
//    }

}


package org.example.domain.pay.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.config.kafka.consumer.MenuResponseConsumer;
import org.example.domain.config.kafka.dto.MenuDto;
import org.example.domain.config.kafka.producer.MenuRequestProducer;
import org.example.domain.entity.*;
import org.example.domain.pay.dto.request.CancelPaymentRequest;
import org.example.domain.pay.dto.request.ConfirmPaymentRequest;
import org.example.domain.pay.dto.request.PreparePaymentRequest;
import org.example.domain.pay.dto.response.ChargeResponse;
import org.example.domain.pay.dto.response.PaymentPrepareResponse;
import org.example.domain.cart.repository.CartRepository;
import org.example.domain.pay.repository.OrdersRepository;
import org.example.domain.pay.repository.TossPaymentRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    @Value("${payment.client.key}")
    private String clientKey;

    @Value("${payment.secret.key}")
    private String API_SECRET_KEY;

    private final MenuRequestProducer menuRequestProducer;
    private final MenuResponseConsumer menuResponseConsumer;
    private final CartRepository cartRepository;
    private final OrdersRepository ordersRepository;
    private final TossPaymentRepository tossPaymentRepository;

    // 결제 준비
    public PaymentPrepareResponse preparePayment(PreparePaymentRequest req) throws Exception {
        // 1. cart 조회
        Cart cart = cartRepository.findById(req.cartId())
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));

        // 2. 요청 ID 생성
        String orderId = UUID.randomUUID().toString();
        String customerKey = cart.getCustomerKey();

        // 3. Kafka로 메뉴 조회 요청 발행
        List<Long> menuIds = cart.getItems().stream()
                .map(CartItem::getMenuId)
                .toList();
        menuRequestProducer.sendMenuRequest(orderId, menuIds);

        // 4. 응답 Future 등록 & 대기
        CompletableFuture<List<MenuDto>> future = menuResponseConsumer.registerRequest(orderId);
        List<MenuDto> menus = future.get(3, TimeUnit.SECONDS); // 3초 대기 (timeout 설정 가능)

        // 5. DTO 변환
        List<PaymentPrepareResponse.CartItemDto> items = cart.getItems().stream()
                .map(item -> {
                    MenuDto menuDto = menus.stream()
                            .filter(m -> m.getMenuId().equals(item.getMenuId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("메뉴 정보를 찾을 수 없습니다."));
                    return PaymentPrepareResponse.CartItemDto.builder()
                            .cartItemId(item.getId())
                            .menuId(menuDto.getMenuId())
                            .menuName(menuDto.getMenuName())
                            .menuCategory(menuDto.getMenuCategory())
                            .amount(item.getQuantity().intValue())
                            .price(menuDto.getMenuPrice())
                            .build();
                })
                .toList();

        Long totalPrice = items.stream()
                .mapToLong(i -> (long) i.getAmount() * i.getPrice())
                .sum();

        // 5. Orders 엔티티 생성 (status = PENDING)
        Orders orders = Orders.builder()
                .orderId(orderId)
                .storeId(req.storeId())
                .tableId(req.tableId())
                .customerKey(customerKey)
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .requestNote(req.requestNote())
                .build();

        // 6. OrderItem 엔티티 생성 및 Orders와 연결
        items.forEach(dto -> {
            OrderItem orderItem = OrderItem.builder()
                    .menuId(dto.getMenuId())
                    .menuName(dto.getMenuName())
                    .menuPrice((long) dto.getPrice())
                    .menuCategory(dto.getMenuCategory())
                    .orderQuantity((long) dto.getAmount())
                    .totalMenuPrice((long) dto.getAmount() * dto.getPrice())
                    .build();

            orders.addOrderItem(orderItem); // 양방향 관계 유지
        });

        // 7. 저장 (cascade 때문에 orderItems도 함께 저장됨)
        ordersRepository.save(orders);

        // 6. 결제 준비 응답
        return PaymentPrepareResponse.builder()
                .orderId(orderId)
                .customerKey(customerKey)
                .clientKey(clientKey)
                .storeId(req.storeId())
                .tableId(req.tableId())
                .items(items)
                .totalPrice(totalPrice)
                .build();
    }

    // 요청 데이터 보내고 응답 받아오기
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

    // 토스 api 서버와 http 연결 설정
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

    // 결제 승인
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

    // 결제 취소
    @Transactional
    public ChargeResponse cancelPayment(String paymentKey, CancelPaymentRequest request) throws IOException {
        JSONObject requestObj = new JSONObject();
        requestObj.put("cancelReason", request.cancelReason());

        if (request.cancelAmount() != null) {
            requestObj.put("cancelAmount", request.cancelAmount());
        }

        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
        HttpURLConnection connection = createConnection(API_SECRET_KEY, url);
        connection.setRequestProperty("Idempotency-Key", UUID.randomUUID().toString());

        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestObj.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject response;
        try (InputStream responseStream = connection.getResponseCode() == 200
                ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            response = (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            throw new RuntimeException("결제 취소 응답 파싱 실패", e);
        }

        if (response.containsKey("error")) {
            throw new RuntimeException("토스 결제 취소 실패: " + response.get("error"));
        }

        String orderId = (String) response.get("orderId");
        JSONArray cancels = (JSONArray) response.get("cancels");
        JSONObject cancelObj = (JSONObject) cancels.get(0);

        String cancelReason = (String) cancelObj.get("cancelReason");
        Long cancelAmount = Long.valueOf(cancelObj.get("cancelAmount").toString());

        // === DB 반영 ===
        Orders orders = ordersRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        orders.cancel();

        TossPayment tossPayment = tossPaymentRepository.findByTossPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        tossPayment.cancel(cancelReason);

        return new ChargeResponse(cancelAmount, "결제 취소 성공");
    }

}

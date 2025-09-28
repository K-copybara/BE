package org.example.domain.pay.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.pay.dto.request.ConfirmPaymentRequest;
import org.example.domain.pay.dto.response.ChargeResponse;
import org.example.domain.pay.service.PaymentService;
import org.example.domain.pay.service.PointService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PointController {

    @Value("${payment.client.key}")
    private String CLIENT_KEY;

    private final PaymentService paymentService;
    private final PointService pointService;

    /**
     * 결제 페이지 호출 (checkout.html 렌더링)
     * 예: http://localhost:8080/v1/payments?amount=1
     */
    @GetMapping
    public String getPaymentPage(@RequestParam int amount, Model model) {
        // 주문 고유 ID 생성
        String orderId = UUID.randomUUID().toString().substring(0, 12);

        // 고객을 식별할 랜덤 키 (비회원이라 DB User 없음)
        long customerKey = new Random().nextLong();
      //  String customerKey = UUID.randomUUID().toString();

        // 모델에 데이터 담기 (checkout.html에서 JS로 사용)
        model.addAttribute("clientKey", CLIENT_KEY);
        model.addAttribute("amount", amount);
        model.addAttribute("orderId", orderId);
        model.addAttribute("customerKey", customerKey);

        // DB/세션에 orderId, amount 저장 (비회원 결제에도 무조건 필요)
        paymentService.createPayment(orderId, customerKey, amount);

        return "payment/checkout"; // → templates/payment/checkout.html
    }

    /**
     * 결제 승인
     * 결제 수단 인증 후 success.html 에서 호출하는 api
     */
    @PostMapping("/confirm")
    @ResponseBody
    public ChargeResponse confirmPayment(@RequestBody ConfirmPaymentRequest confirmPaymentRequest) throws IOException {
        // PointService 에 ConfirmPaymentRequest 넘겨주도록 수정
        return pointService.confirmPayment(confirmPaymentRequest);
    }

}

//public class PointController {
//
//    @Value("${payment.client.key}")
//    private String clientKey;
//
//    private final PaymentService paymentService;
//
//    /**
//     * 결제 페이지 호출 (checkout.html 렌더링)
//     * 예: http://localhost:8080/points/charge?userId=1&amount=10000
//     */
//    @GetMapping("/points/charge")
//    public String getPaymentPage(@RequestParam Long userId,
//                                 @RequestParam int amount,
//                                 Model model) {
//
//        // 주문 고유 ID 생성
//        String orderId = UUID.randomUUID().toString().substring(0, 12);
//
//        // 모델에 데이터 담기 (checkout.html에서 JS로 사용)
//        model.addAttribute("userId", userId);
//        model.addAttribute("clientKey", clientKey);
//        model.addAttribute("amount", amount);
//        model.addAttribute("orderId", orderId);
//
//        // 결제 임시 저장 (DB/세션에 orderId, 금액 저장)
//        paymentService.createPayment(orderId, userId, amount);
//
//        return "checkout"; // → templates/checkout.html 렌더링
//    }
//}
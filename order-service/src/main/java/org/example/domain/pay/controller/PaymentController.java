package org.example.domain.pay.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.pay.dto.request.CancelPaymentRequest;
import org.example.domain.pay.dto.request.ConfirmPaymentRequest;
import org.example.domain.pay.dto.request.PreparePaymentRequest;
import org.example.domain.pay.dto.response.ChargeResponse;
import org.example.domain.pay.dto.response.PaymentPrepareResponse;
import org.example.domain.pay.service.PaymentService;
import org.example.dto.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PaymentController {

    @Value("${payment.client.key}")
    private String CLIENT_KEY;

    private final PaymentService paymentService;

    /**
     * 결제 준비
     */
    @PostMapping("/prepare")
    public Response<PaymentPrepareResponse> preparePayment(
            @RequestBody PreparePaymentRequest request
    ) throws Exception {
        PaymentPrepareResponse response = paymentService.preparePayment(request);
        return Response.success("결제 준비 성공", response);
    }

    /**
     * 결제 승인
     * 결제 수단 인증 후 success.html 에서 호출하는 api
     */
    @PostMapping("/confirm")
    @ResponseBody
    public ChargeResponse confirmPayment(
            @RequestBody ConfirmPaymentRequest confirmPaymentRequest
    ) throws IOException {
        // PointService 에 ConfirmPaymentRequest 넘겨주도록 수정
        return paymentService.confirmPayment(confirmPaymentRequest);
    }

    /**
     * 결제 취소
     * @param paymentKey 결제 고유 키 (토스 제공)
     * @param request    취소 요청 정보 (사유, 금액)
     */
    @PostMapping("/{paymentKey}/cancel")
    @ResponseBody
    public ChargeResponse cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody CancelPaymentRequest request
    ) throws IOException {
        return paymentService.cancelPayment(paymentKey, request);
    }


}


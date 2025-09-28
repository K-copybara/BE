package org.example.domain.pay.dto.request;

public record ConfirmPaymentRequest(
        String paymentKey,
        String orderId,
        Integer amount
) {}


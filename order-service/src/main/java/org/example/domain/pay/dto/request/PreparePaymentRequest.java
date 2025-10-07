package org.example.domain.pay.dto.request;


public record PreparePaymentRequest(
        Long cartId,
        Long storeId,
        Long tableId,
        String requestNote
) {}
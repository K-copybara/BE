package org.example.domain.pay.dto.request;

import lombok.Builder;

@Builder
public record CancelPaymentRequest(
        String cancelReason,
        Long cancelAmount
) {}

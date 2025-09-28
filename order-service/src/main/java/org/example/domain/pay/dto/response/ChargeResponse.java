package org.example.domain.pay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChargeResponse {
    private Long chargedAmount;
    private String message;
}

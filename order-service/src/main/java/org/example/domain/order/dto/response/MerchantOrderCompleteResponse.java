package org.example.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MerchantOrderCompleteResponse {
    private boolean success;
    private String message;

    public static MerchantOrderCompleteResponse success() {
        return MerchantOrderCompleteResponse.builder()
                .success(true)
                .message("주문 완료")
                .build();
    }
}

package org.example.domain.pay.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PaymentPrepareResponse {
    private String orderId;
    private String customerKey;
    private String clientKey;
    private Long storeId;
    private Long tableId;
    private List<CartItemDto> items;
    private Long totalPrice;

    @Getter
    @Builder
    public static class CartItemDto {
        private Long cartItemId;
        private Long menuId;
        private String menuName;
        private String menuCategory;
        private int amount;
        private int price;
    }
}

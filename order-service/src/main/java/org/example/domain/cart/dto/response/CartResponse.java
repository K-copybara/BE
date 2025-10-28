package org.example.domain.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.domain.entity.Cart;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CartResponse {
    private Long cartId;
    private Long storeId;
    private String customerKey;
    private List<CartItemResponse> items;
    private Long totalPrice;

    public static CartResponse of(Cart cart, List<CartItemResponse> items) {
        long totalPrice = items.stream()
                .mapToLong(i -> i.getAmount() * i.getPrice())
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .storeId(cart.getStoreId())
                .customerKey(cart.getCustomerKey())
                .items(items)
                .totalPrice(totalPrice)
                .build();
    }
}


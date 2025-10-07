package org.example.domain.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.entity.CartItem;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartResponse {
    private Long cartItemId;
    private Long menuId;
    private int amount;

    public static AddToCartResponse fromEntity(CartItem item) {
        return AddToCartResponse.builder()
                .cartItemId(item.getId())
                .menuId(item.getMenuId())
                .amount(item.getQuantity().intValue())
                .build();
    }
}

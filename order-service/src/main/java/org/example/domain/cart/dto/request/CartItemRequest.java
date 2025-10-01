package org.example.domain.cart.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.entity.Cart;
import org.example.domain.entity.CartItem;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    private Long storeId;
    private Long tableId;
    private Long menuId;
    private int amount;
    private String customerKey;
}

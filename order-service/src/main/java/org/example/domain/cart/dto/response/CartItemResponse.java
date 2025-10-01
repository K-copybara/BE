package org.example.domain.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.config.kafka.dto.CartMenuDto;
import org.example.domain.config.kafka.dto.MenuDto;
import org.example.domain.entity.CartItem;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private Long cartItemId;
    private Long menuId;
    private String menuName;
    private String menuCategory;
    private String menuPicture;
    private Long amount;
    private Long price;

    // MenuDto → CartMenuDto로 변경
    public static CartItemResponse fromEntity(CartItem item, CartMenuDto menuDto) {
        return CartItemResponse.builder()
                .cartItemId(item.getId())
                .menuId(menuDto.getMenuId())
                .menuName(menuDto.getMenuName())
                .menuCategory(menuDto.getMenuCategory())
                .menuPicture(menuDto.getMenuPicture())
                .amount(item.getQuantity())
                .price(menuDto.getMenuPrice())
                .build();
    }
}

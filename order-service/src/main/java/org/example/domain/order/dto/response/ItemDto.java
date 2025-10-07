package org.example.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long menuId;
    private String menuName;
    private Long amount;
    private Long price;
    private Long totalPrice;
}

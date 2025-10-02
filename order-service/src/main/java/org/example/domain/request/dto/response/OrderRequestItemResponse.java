package org.example.domain.request.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.entity.OrderRequestItem;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestItemResponse {
    private Long orderRequestItemId;
    private Long menuId;
    private Long amount;

    public static OrderRequestItemResponse fromEntity(OrderRequestItem item) {
        return OrderRequestItemResponse.builder()
                .orderRequestItemId(item.getId())
                .menuId(item.getMenuId())
                .amount(item.getAmount())
                .build();
    }
}

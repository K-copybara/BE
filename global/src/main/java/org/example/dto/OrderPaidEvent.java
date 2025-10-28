package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPaidEvent {
    private String orderId;
    private Long storeId;
    private Long tableId;
    private LocalDateTime orderedAt;
    private String requestNote;
    private List<OrderItemDto> items;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderItemDto {
        private Long menuId;
        private String menuName;
        private int amount;
    }
}

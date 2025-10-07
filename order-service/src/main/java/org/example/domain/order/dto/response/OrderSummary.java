package org.example.domain.order.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummary {
    private String orderId;
    private Long totalPrice;
    private String requestNote;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<ItemDto> items;
}

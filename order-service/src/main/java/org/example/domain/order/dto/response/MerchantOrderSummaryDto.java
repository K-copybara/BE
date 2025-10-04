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
public class MerchantOrderSummaryDto {
    private String orderId;
    private Long tableId;
    private LocalDateTime orderedAt;
    private OrderStatus status;
    private String requestNote;
    private List<MerchantOrderItemDto> items;
}

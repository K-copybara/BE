package org.example.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistoryResponse {
    private Long storeId;
    private Long totalOrders;
    private Long totalSpent;
    private List<OrderSummary> orders;
}

package org.example.domain.request.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.entity.OrderRequest;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestResponse {
    private Long orderRequestId;
    private Long storeId;
    private String customerKey;
    private String requestNote;
    private List<OrderRequestItemResponse> items;

    public static OrderRequestResponse fromEntity(OrderRequest orderRequest) {
        return OrderRequestResponse.builder()
                .orderRequestId(orderRequest.getId())
                .storeId(orderRequest.getStoreId())
                .customerKey(orderRequest.getCustomerKey())
                .requestNote(orderRequest.getRequestNote())
                .items(orderRequest.getItems().stream()
                        .map(OrderRequestItemResponse::fromEntity)
                        .toList())
                .build();
    }
}

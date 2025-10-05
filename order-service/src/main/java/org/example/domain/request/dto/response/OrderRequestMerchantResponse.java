package org.example.domain.request.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.entity.OrderRequest;
import org.example.domain.request.dto.request.OrderRequestItemReadDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestMerchantResponse {
    private Long requestId;
    private Long tableId;
    private LocalDateTime requestedAt;
    private String status;
    private String requestNote;
    private List<OrderRequestItemReadDto> items;

    public static OrderRequestMerchantResponse from(OrderRequest entity) {
        return OrderRequestMerchantResponse.builder()
                .requestId(entity.getId())
                .tableId(entity.getTableId())
                .requestedAt(entity.getCreatedAt())
                .status(entity.getRequestStatus().name())
                .requestNote(entity.getRequestNote())
                .items(entity.getItems().stream()
                        .map(OrderRequestItemReadDto::from)
                        .toList())
                .build();
    }
}


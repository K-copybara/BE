package org.example.domain.request.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {
    private Long storeId;
    private String customerKey;
    private String requestNote; // optional
    private List<OrderRequestItemDto> items;
}

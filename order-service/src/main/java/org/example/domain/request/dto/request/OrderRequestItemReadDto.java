package org.example.domain.request.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.entity.OrderRequestItem;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestItemReadDto {
    private String name;
    private Long amount;

    public static OrderRequestItemReadDto from(OrderRequestItem item) {
        return OrderRequestItemReadDto.builder()
                .name(item.getMenuName())
                .amount(item.getAmount())
                .build();
    }
}

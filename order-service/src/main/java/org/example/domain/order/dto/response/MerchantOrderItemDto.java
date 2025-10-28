package org.example.domain.order.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantOrderItemDto {
    private Long menuId;
    private String menuName;
    private Long amount;
}

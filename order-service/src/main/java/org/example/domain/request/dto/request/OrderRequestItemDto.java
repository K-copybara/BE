package org.example.domain.request.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestItemDto {
    private Long menuId;
    private String menuName;
    private Long amount;
}
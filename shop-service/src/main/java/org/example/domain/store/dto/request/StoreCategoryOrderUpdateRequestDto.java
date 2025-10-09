package org.example.domain.store.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StoreCategoryOrderUpdateRequestDto {
    private List<CategoryOrderDto> categoryOrders;
}

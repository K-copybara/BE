package org.example.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreCategoryResponseDto {
    private final Long categoryId;
    private final String name;
    private final Long order;
    private final Long menuCount;
}

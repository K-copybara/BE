package org.example.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreCategoryCreateResponseDto {
    private final Long categoryId;
}

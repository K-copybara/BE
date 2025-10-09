package org.example.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoreResponseDto {
    private final Long storeId;
    private final String shopName;
    private final String notice;
    private final List<BusinessHoursDetailDto> businessHoursDetail;
}

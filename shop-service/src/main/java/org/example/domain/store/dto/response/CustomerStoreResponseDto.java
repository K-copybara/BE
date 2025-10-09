package org.example.domain.store.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerStoreResponseDto {
    private final Long storeId;
    private final Long tableId;
    private final String shopName;
    private final String notice;
}

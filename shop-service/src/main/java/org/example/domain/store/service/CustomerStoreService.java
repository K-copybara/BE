package org.example.domain.store.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.entity.Store;
import org.example.domain.store.dto.response.CustomerStoreResponseDto;
import org.example.domain.store.repository.StoreRepository;
import org.example.dto.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerStoreService {

    private final StoreRepository storeRepository;

    // 고객 가게 정보 조회
    public Response<CustomerStoreResponseDto> getCustomerStoreInfo(Long storeId, Long tableId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        CustomerStoreResponseDto dto = CustomerStoreResponseDto.builder()
                .storeId(store.getId())
                .tableId(tableId)
                .shopName(store.getShopName())
                .notice(store.getNotice())
                .build();

        return Response.success("가게 메인 정보 조회 성공", dto);
    }
}

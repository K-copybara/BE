package org.example.domain.store.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.entity.MenuCategory;
import org.example.domain.entity.Store;
import org.example.domain.store.dto.response.CustomerStoreResponseDto;
import org.example.domain.store.dto.response.StoreCategorySimpleResponseDto;
import org.example.domain.store.repository.MenuCategoryRepository;
import org.example.domain.store.repository.StoreRepository;
import org.example.dto.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerStoreService {

    private final StoreRepository storeRepository;
    private final MenuCategoryRepository menuCategoryRepository;

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

    // 고객 카테고리 조회
    @Transactional(readOnly = true)
    public Response<List<StoreCategorySimpleResponseDto>> getStoreCategories(Long storeId) {
        // 상점 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        // 카테고리 조회 (기본카테고리 제외)
        List<MenuCategory> categories =
                menuCategoryRepository.findByStoreOrderByOrderedIndexAsc(store);

        // DTO 변환
        List<StoreCategorySimpleResponseDto> categoryDtos = categories.stream()
                .map(category -> new StoreCategorySimpleResponseDto(
                        category.getId(),
                        category.getCategoryName()
                ))
                .collect(Collectors.toList());

        // 응답
        return Response.success("카테고리 목록 조회 성공", categoryDtos);
    }
}

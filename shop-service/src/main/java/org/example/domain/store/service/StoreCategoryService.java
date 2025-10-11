package org.example.domain.store.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.config.kafka.producer.StoreEventProducer;
import org.example.domain.entity.MenuCategory;
import org.example.domain.entity.Store;
import org.example.domain.store.dto.request.CategoryOrderDto;
import org.example.domain.store.dto.request.StoreCategoryCreateRequestDto;
import org.example.domain.store.dto.request.StoreCategoryOrderUpdateRequestDto;
import org.example.domain.store.dto.response.StoreCategoryCreateResponseDto;
import org.example.domain.store.dto.response.StoreCategoryResponseDto;
import org.example.domain.menu.repository.MenuCategoryRepository;
import org.example.domain.store.repository.StoreRepository;
import org.example.dto.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreCategoryService {

    private final StoreRepository storeRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final StoreEventProducer storeEventProducer;

    // 상점 카테고리 조회
    @Transactional(readOnly = true)
    public Response<List<StoreCategoryResponseDto>> getCategories(String email) {
        // 사장 이메일로 상점 조회
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        // 카테고리 목록 조회 (orderedIndex 기준 오름차순)
        List<MenuCategory> categories = menuCategoryRepository.findByStoreOrderByOrderedIndexAsc(store);

        // DTO 매핑
        List<StoreCategoryResponseDto> responseDtos = categories.stream()
                .map(category -> StoreCategoryResponseDto.builder()
                        .categoryId(category.getId())
                        .name(category.getCategoryName())
                        .order(category.getOrderedIndex())
                        .menuCount((long) category.getMenus().size()) // 메뉴 개수
                        .build())
                .collect(Collectors.toList());

        return Response.success("카테고리 조회 성공", responseDtos);
    }

    // 카테고리 생성
    @Transactional
    public Response<StoreCategoryCreateResponseDto> createCategory(String email, StoreCategoryCreateRequestDto requestDto) {
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        // 요청사항 카테고리 이름은 금지
        if ("요청사항".equals(requestDto.getName())) {
            throw new IllegalArgumentException("요청사항 카테고리는 직접 생성할 수 없습니다.");
        }

        MenuCategory category = MenuCategory.builder()
                .store(store)
                .categoryName(requestDto.getName())
                .orderedIndex(store.getCategories().size() + 1L)
                .isDefault(false)
                .build();

        menuCategoryRepository.save(category);

        // AI 서버 자동 업데이트 이벤트 발행
        storeEventProducer.sendStoreUpdatedEvent(store.getId(), "UPDATED");

        return Response.success("카테고리 생성 성공",
                StoreCategoryCreateResponseDto.builder().categoryId(category.getId()).build());
    }

    // 카테고리 순서 수정
    @Transactional
    public Response<List<StoreCategoryResponseDto>> updateCategoryOrder(
            String email,
            StoreCategoryOrderUpdateRequestDto requestDto
    ) {
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        Map<Long, Long> orderMap = requestDto.getCategoryOrders().stream()
                .collect(Collectors.toMap(CategoryOrderDto::getCategoryId, CategoryOrderDto::getOrder));

        List<MenuCategory> categories = menuCategoryRepository.findByStoreOrderByOrderedIndexAsc(store);

        categories.stream()
                .filter(category -> orderMap.containsKey(category.getId()))
                .forEach(category -> category.changeOrder(orderMap.get(category.getId())));

        // AI 서버 자동 업데이트 이벤트 발행
        storeEventProducer.sendStoreUpdatedEvent(store.getId(), "UPDATED");

        List<StoreCategoryResponseDto> result = categories.stream()
                .sorted(Comparator.comparing(MenuCategory::getOrderedIndex))
                .map(category -> StoreCategoryResponseDto.builder()
                        .categoryId(category.getId())
                        .name(category.getCategoryName())
                        .order(category.getOrderedIndex())
                        .menuCount((long) category.getMenus().size())
                        .build())
                .collect(Collectors.toList());

        return Response.success("카테고리 순서 변경 성공", result);
    }

    // 카테고리 삭제
    @Transactional
    public Response<Void> deleteCategory(String email, Long categoryId) {
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        store.validateOwnership(category);     // 상점 소유자 검증
        category.validateDeletable();          // 삭제 가능 여부 검증

        menuCategoryRepository.delete(category);

        // AI 서버 자동 업데이트 이벤트 발행
        storeEventProducer.sendStoreUpdatedEvent(store.getId(), "UPDATED");

        return Response.success("카테고리 삭제 성공", null);
    }


}

package org.example.domain.menu.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.entity.Menu;
import org.example.domain.entity.Store;
import org.example.domain.menu.dto.response.CustomerMenuDetailResponseDto;
import org.example.domain.menu.dto.response.CustomerMenuListResponseDto;
import org.example.domain.menu.repository.MenuRepository;
import org.example.domain.store.repository.StoreRepository;
import org.example.dto.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerMenuService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    // 상세 메뉴 조회
    @Transactional(readOnly = true)
    public Response<CustomerMenuDetailResponseDto> getMenuDetail(Long storeId, Long menuId) {

        // 상점 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        // 메뉴 조회 + 상점 일치 여부 검증
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        if (!menu.getCategory().getStore().getId().equals(storeId)) {
            throw new IllegalArgumentException("해당 상점의 메뉴가 아닙니다.");
        }

        // DTO 변환
        CustomerMenuDetailResponseDto dto = CustomerMenuDetailResponseDto.builder()
                .menuId(menu.getId())
                .menuName(menu.getMenuName())
                .menuPrice(menu.getMenuPrice())
                .menuInfo(menu.getMenuInfo())
                .menuPicture(menu.getMenuPicture())
                .build();

        return Response.success("메뉴 상세 조회 성공", dto);
    }

    // 전체 메뉴 조회
    @Transactional(readOnly = true)
    public Response<List<CustomerMenuListResponseDto>> getAllMenusByStore(Long storeId) {

        // 상점 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        // 해당 상점의 전체 메뉴 조회 (카테고리 순서 + 등록순)
        List<Menu> menus = menuRepository.findByCategory_StoreOrderByCategory_OrderedIndexAscCreatedAtAsc(store);

        // DTO 변환
        List<CustomerMenuListResponseDto> responseDtos = menus.stream()
                .map(menu -> CustomerMenuListResponseDto.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getMenuName())
                        .menuInfo(menu.getMenuInfo())
                        .menuPrice(menu.getMenuPrice())
                        .menuPicture(menu.getMenuPicture())
                        .category(new CustomerMenuListResponseDto.CategoryDto(
                                menu.getCategory().getId(),
                                menu.getCategory().getCategoryName()
                        ))
                        .build())
                .toList();

        return Response.success("전체 메뉴 조회 성공", responseDtos);
    }
}


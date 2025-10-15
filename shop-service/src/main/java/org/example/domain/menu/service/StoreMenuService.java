package org.example.domain.menu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.config.kafka.producer.MenuEventProducer;
import org.example.domain.config.s3.S3UploadService;
import org.example.domain.entity.Menu;
import org.example.domain.entity.MenuCategory;
import org.example.domain.entity.Store;
import org.example.domain.menu.dto.request.StoreMenuCreateRequestDto;
import org.example.domain.menu.dto.request.StoreMenuUpdateRequestDto;
import org.example.domain.menu.dto.response.StoreMenuDetailResponseDto;
import org.example.domain.menu.dto.response.StoreMenuResponseDto;
import org.example.domain.menu.repository.MenuRepository;
import org.example.domain.menu.repository.MenuCategoryRepository;
import org.example.domain.store.repository.StoreRepository;
import org.example.dto.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreMenuService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuEventProducer menuEventProducer;
    private final S3UploadService s3UploadService;

    // 상점 메뉴 전체 조회
    @Transactional(readOnly = true)
    public Response<List<StoreMenuResponseDto>> getStoreMenus(String email) {

        // 상점 조회 (인증된 사장님 기준)
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        // 해당 상점의 모든 메뉴 조회
        List<Menu> menus = menuRepository.findByCategory_StoreOrderByCategory_OrderedIndexAscCreatedAtAsc(store);

        // DTO 변환
        List<StoreMenuResponseDto> menuDtos = menus.stream()
                .map(menu -> StoreMenuResponseDto.builder()
                        .menuId(menu.getId())
                        .name(menu.getMenuName())
                        .menuInfo(menu.getMenuInfo())
                        .price(menu.getMenuPrice())
                        .status(menu.getMenuStatus() ? "ON_SALE" : "SOLD_OUT")
                        .imageUrl(menu.getMenuPicture()) // 업로드된 URL
                        .build())
                .collect(Collectors.toList());

        return Response.success("메뉴 조회 성공", menuDtos);
    }

    // 메뉴 상세 정보 불러오기
    @Transactional(readOnly = true)
    public Response<StoreMenuDetailResponseDto> getMenuDetail(String email, Long menuId) {

        // 사장님의 상점 조회
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        // 메뉴 조회
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        // 소유 상점 검증
        if (!menu.getCategory().getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException("본인 상점의 메뉴만 조회할 수 있습니다.");
        }

        // DTO 변환
        StoreMenuDetailResponseDto dto = StoreMenuDetailResponseDto.builder()
                .menuId(menu.getId())
                .menuName(menu.getMenuName())
                .menuPrice(menu.getMenuPrice())
                .menuInfo(menu.getMenuInfo())
                .menuPicture(menu.getMenuPicture())
                .menuStatus(menu.getMenuStatus() ? "ON_SALE" : "SOLD_OUT")
                .spicyLevel(menu.getSpicy())
                .allergies(
                        (menu.getAllergy() == null || menu.getAllergy().isBlank())
                                ? List.of()
                                : Arrays.asList(menu.getAllergy().split(","))
                )
                .extraInfo(menu.getExtraInfo())
                .category(
                        new StoreMenuDetailResponseDto.CategoryDto(
                                menu.getCategory().getId(),
                                menu.getCategory().getCategoryName()
                        )
                )
                .build();

        return Response.success("메뉴 상세 조회 성공", dto);
    }

    // 메뉴 생성
    @Transactional
    public Response<Void> createMenu(String email, MultipartFile image, StoreMenuCreateRequestDto requestDto) {

        log.info("📩 MultipartFile 전달 여부: {}", (image != null ? image.getOriginalFilename() : "null"));

        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        MenuCategory category = menuCategoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        // 이미지 업로드 (임시 Mock URL)
        String imageUrl = null;
        try {
            if (image != null && !image.isEmpty()) {
                imageUrl = s3UploadService.saveFile(image);
            }
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }

        // 알러지 문자열 변환
        String allergies = (requestDto.getAllergies() == null || requestDto.getAllergies().isEmpty())
                ? ""
                : String.join(",", requestDto.getAllergies());

        // 메뉴 저장
        Menu menu = Menu.builder()
                .category(category)
                .menuName(requestDto.getName())
                .menuPrice(requestDto.getPrice())
                .menuInfo(requestDto.getDescription())
                .menuPicture(imageUrl)
                .menuStatus(true)
                .spicy(requestDto.getSpicyLevel())
                .allergy(allergies)
                .extraInfo(requestDto.getExtraInfo())
                .build();

        menuRepository.save(menu);

        // AI 서버로 변경 사항 전송
        menuEventProducer.sendMenuEvent(store.getId(), menu.getId(), "CREATED", menu);

        return Response.success("메뉴 등록 성공", null);
    }

    // 메뉴 수정
    @Transactional
    public Response<Void> updateMenu(String email, Long menuId, MultipartFile image, StoreMenuUpdateRequestDto requestDto) {

        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        // 상점 소유권 검증 (보안)
        if (!menu.getCategory().getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException("본인 상점의 메뉴만 수정할 수 있습니다.");
        }

        // 카테고리 변경
        if (requestDto.getCategoryId() != null && !requestDto.getCategoryId().equals(menu.getCategory().getId())) {
            MenuCategory newCategory = menuCategoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
            menu.changeCategory(newCategory);
        }

        // 필드 수정
        menu.changeName(requestDto.getName());
        menu.changePrice(requestDto.getPrice());
        menu.changeInfo(requestDto.getDescription());
        menu.changeSpicy(requestDto.getSpicyLevel());
        menu.changeAllergy(String.join(",", requestDto.getAllergies()));
        menu.changeExtraInfo(requestDto.getExtraInfo());

        // 이미지 처리 로직
        if (requestDto.isRemoveImage()) {
            if (menu.getMenuPicture() != null) {
                s3UploadService.deleteImage(menu.getMenuPicture());
                menu.removeImage();
            }
        } else if (image != null && !image.isEmpty()) {
            if (menu.getMenuPicture() != null) {
                s3UploadService.deleteImage(menu.getMenuPicture()); // 기존 이미지 삭제
            }
            try {
                String newImageUrl = s3UploadService.saveFile(image);
                menu.changeImage(newImageUrl);
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 실패", e);
            }
        }

        // AI 서버로 변경 사항 전송
        menuEventProducer.sendMenuEvent(store.getId(), menu.getId(), "UPDATED", menu);

        return Response.success("메뉴 수정 성공", null);
    }

    // 메뉴 삭제
    @Transactional
    public Response<Void> deleteMenu(String email, Long menuId) {

        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        if (!menu.getCategory().getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException("본인 상점의 메뉴만 삭제할 수 있습니다.");
        }

        // 이미지 삭제 (현재는 Mock)
        if (menu.getMenuPicture() != null) {
            s3UploadService.deleteImage(menu.getMenuPicture());
        }

        // AI 서버로 변경 사항 전송
        menuEventProducer.sendMenuEvent(store.getId(), menu.getId(), "DELETED", menu);

        menuRepository.delete(menu);
        return Response.success("메뉴 삭제 성공", null);
    }


    // 메뉴 일시품절 처리
    @Transactional
    public Response<Void> toggleMenuSoldOutStatus(String email, Long menuId) {

        // 상점 조회
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        // 메뉴 조회
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        // 소유권 검증
        if (!menu.getCategory().getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException("본인 상점의 메뉴만 수정할 수 있습니다.");
        }

        // 현재 상태 확인 후 토글
        boolean currentStatus = Boolean.TRUE.equals(menu.getMenuStatus());
        if (currentStatus) {
            menu.markAsSoldOut(); // → false로 전환
            // AI 서버로 변경 사항 전송
            menuEventProducer.sendMenuEvent(store.getId(), menu.getId(), "STATUS_CHANGED", menu);
            return Response.success("일시 품절 설정 성공", null);
        } else {
            menu.markAsOnSale(); // → true로 전환
            // AI 서버로 변경 사항 전송
            menuEventProducer.sendMenuEvent(store.getId(), menu.getId(), "STATUS_CHANGED", menu);

            return Response.success("판매 재개 성공", null);
        }
    }


}

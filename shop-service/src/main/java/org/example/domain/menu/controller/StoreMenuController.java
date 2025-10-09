package org.example.domain.menu.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.config.security.CustomUserDetails;
import org.example.domain.menu.dto.request.StoreMenuCreateRequestDto;
import org.example.domain.menu.dto.request.StoreMenuUpdateRequestDto;
import org.example.domain.menu.dto.response.StoreMenuDetailResponseDto;
import org.example.domain.menu.dto.response.StoreMenuResponseDto;
import org.example.domain.menu.service.StoreMenuService;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/store/menu")
public class StoreMenuController {

    private final StoreMenuService storeMenuService;

    // 메뉴 조회
    @GetMapping
    public ResponseEntity<Response<List<StoreMenuResponseDto>>> getMenus(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        String email = user.getEmail();
        Response<List<StoreMenuResponseDto>> response = storeMenuService.getStoreMenus(email);
        return ResponseEntity.ok(response);
    }

    // 메뉴 상세 조회
    @GetMapping("/{menuId}")
    public ResponseEntity<Response<StoreMenuDetailResponseDto>> getMenuDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long menuId
    ) {
        String email = user.getEmail();
        Response<StoreMenuDetailResponseDto> response = storeMenuService.getMenuDetail(email, menuId);
        return ResponseEntity.ok(response);
    }

    // 메뉴 등록
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Response<Void>> createMenu(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart("data") StoreMenuCreateRequestDto requestDto
    ) {
        String email = user.getEmail();
        Response<Void> response = storeMenuService.createMenu(email, image, requestDto);
        return ResponseEntity.ok(response);
    }

    // 메뉴 수정
    @PatchMapping(value = "/{menuId}", consumes = {"multipart/form-data"})
    public ResponseEntity<Response<Void>> updateMenu(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long menuId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart("data") StoreMenuUpdateRequestDto requestDto
    ) {
        String email = user.getEmail();
        Response<Void> response = storeMenuService.updateMenu(email, menuId, image, requestDto);
        return ResponseEntity.ok(response);
    }

    // 메뉴 삭제
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Response<Void>> deleteMenu(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long menuId
    ) {
        String email = user.getEmail();
        Response<Void> response = storeMenuService.deleteMenu(email, menuId);
        return ResponseEntity.ok(response);
    }

    // 메뉴 일시품절 처리
    @PatchMapping("/{menuId}/soldout")
    public ResponseEntity<Response<Void>> toggleSoldOutStatus(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long menuId
    ) {
        String email = user.getEmail();
        Response<Void> response = storeMenuService.toggleMenuSoldOutStatus(email, menuId);
        return ResponseEntity.ok(response);
    }


}
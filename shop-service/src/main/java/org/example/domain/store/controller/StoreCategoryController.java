package org.example.domain.store.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.config.security.CustomUserDetails;
import org.example.domain.store.dto.request.StoreCategoryCreateRequestDto;
import org.example.domain.store.dto.request.StoreCategoryOrderUpdateRequestDto;
import org.example.domain.store.dto.response.StoreCategoryCreateResponseDto;
import org.example.domain.store.dto.response.StoreCategoryResponseDto;
import org.example.domain.store.service.StoreCategoryService;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/store/category")
public class StoreCategoryController {

    private final StoreCategoryService storeCategoryService;

    // 상점 카테고리 조회
    @GetMapping
    public ResponseEntity<Response<List<StoreCategoryResponseDto>>> getCategories(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        String email = user.getEmail();
        Response<List<StoreCategoryResponseDto>> response = storeCategoryService.getCategories(email);
        return ResponseEntity.ok(response);
    }

    // 카테고리 생성
    @PostMapping
    public ResponseEntity<Response<StoreCategoryCreateResponseDto>> addCategory(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody StoreCategoryCreateRequestDto requestDto
    ) {
        String email = user.getEmail();
        Response<StoreCategoryCreateResponseDto> response = storeCategoryService.createCategory(email, requestDto);
        return ResponseEntity.ok(response);
    }

    // 카테고리 순서 수정
    @PatchMapping("/order")
    public ResponseEntity<Response<List<StoreCategoryResponseDto>>> updateCategoryOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody StoreCategoryOrderUpdateRequestDto requestDto
    ) {
        String email = user.getEmail();
        Response<List<StoreCategoryResponseDto>> response =
                storeCategoryService.updateCategoryOrder(email, requestDto);
        return ResponseEntity.ok(response);
    }

    // 카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Response<Void>> deleteCategory(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long categoryId
    ) {
        String email = user.getEmail();
        Response<Void> response = storeCategoryService.deleteCategory(email, categoryId);
        return ResponseEntity.ok(response);
    }
}

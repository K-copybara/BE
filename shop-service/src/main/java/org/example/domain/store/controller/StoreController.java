package org.example.domain.store.controller;


import lombok.RequiredArgsConstructor;
import org.example.domain.config.security.CustomUserDetails;
import org.example.domain.store.dto.request.StoreBusinessHoursUpdateRequestDto;
import org.example.domain.store.dto.request.StoreNoticeUpdateRequestDto;
import org.example.domain.store.dto.response.StoreResponseDto;
import org.example.domain.store.service.StoreService;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/store")
public class StoreController {

    private final StoreService storeService;

    // 상점 정보 조회
    @GetMapping("/me")
    public ResponseEntity<Response<StoreResponseDto>> getMyStoreInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String email = userDetails.getEmail();
        StoreResponseDto dto = storeService.getStoreInfoByEmail(email);
        return ResponseEntity.ok(Response.success("상점 정보를 조회 성공", dto));
    }

    // 공지사항 수정
    @PatchMapping("/notice")
    public ResponseEntity<Response<Void>> updateNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody StoreNoticeUpdateRequestDto requestDto
    ) {
        String email = userDetails.getEmail();
        Response<Void> response = storeService.updateStoreNotice(email, requestDto);
        return ResponseEntity.ok(response);
    }

    // 영업시간 수정
    @PatchMapping("/hours")
    public ResponseEntity<Response<Void>> updateBusinessHours(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody StoreBusinessHoursUpdateRequestDto requestDto
    ) {
        String email = userDetails.getEmail();
        Response<Void> response = storeService.updateBusinessHours(email, requestDto);
        return ResponseEntity.ok(response);
    }
}

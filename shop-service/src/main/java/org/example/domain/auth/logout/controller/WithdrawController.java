package org.example.domain.auth.logout.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.domain.auth.logout.service.WithdrawService;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/auth")
public class WithdrawController {

    private final WithdrawService withdrawService;

    // 회원 탈퇴 (상점 비활성화)
    @PostMapping("/withdraw")
    public ResponseEntity<Response<Void>> withdraw(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Bearer 제거
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;
        withdrawService.withdraw(token);
        return ResponseEntity.ok(Response.success("회원 탈퇴 처리 완료", null));
    }
}

package org.example.domain.auth.logout.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.domain.auth.logout.service.LogoutService;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/auth")
public class LogoutController {

    private final LogoutService logoutService;
    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Bearer 제거
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;
        logoutService.logout(token);
        return ResponseEntity.ok(Response.success("로그아웃 성공", null));
    }
}

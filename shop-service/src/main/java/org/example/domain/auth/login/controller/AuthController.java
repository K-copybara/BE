package org.example.domain.auth.login.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.domain.auth.login.dto.SignInDto;
import org.example.domain.auth.login.dto.SignUpDto;
import org.example.domain.auth.login.dto.TokenDto;
import org.example.domain.auth.login.service.AuthService;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/auth")
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/sign-up")
    public ResponseEntity<Response<Void>> signup(@RequestBody SignUpDto signUpDto) {
        authService.signup(signUpDto);
        return ResponseEntity.ok(Response.success("회원가입 성공했습니다.", null));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Response<TokenDto>> signIn(@RequestBody SignInDto signInDto) {
        TokenDto tokenDto = authService.signIn(signInDto);
        return ResponseEntity.ok(Response.success("로그인 성공했습니다.", tokenDto));
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<Response<TokenDto>> reissueToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        TokenDto tokenDto = authService.reissue(bearerToken);
        return ResponseEntity.ok(Response.success("토큰 재발급 성공", tokenDto));
    }
}
package org.example.domain.auth.login.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.auth.login.dto.SignInDto;
import org.example.domain.auth.login.dto.SignUpDto;
import org.example.domain.auth.login.dto.TokenDto;
import org.example.domain.auth.login.repository.StoreRepository;
import org.example.domain.config.jwt.TokenProvider;
import org.example.domain.config.redis.RedisUtil;
import org.example.domain.entity.Store;
import org.example.exception.CustomException;
import org.example.exception.ErrorCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;

    // 회원가입
    @Transactional
    public void signup(SignUpDto signUpDto) {
        if (storeRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Store store = Store.builder()
                .email(signUpDto.getEmail())
                .shopName(signUpDto.getShopName())
                .password(passwordEncoder.encode(signUpDto.getPassword()))
                .phone(signUpDto.getPhone())
                .address(signUpDto.getAddress())
                .status(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        storeRepository.save(store);
        log.info("상점 회원가입 완료: {}", store.getEmail());
    }

    // 로그인
    @Transactional
    public TokenDto signIn(SignInDto signInDto) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(signInDto.getEmail(), signInDto.getPassword());

        try {
            log.info("로그인 시도: {}", signInDto.getEmail());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            log.info("로그인 성공: {}", authentication.getName());

            TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
            redisUtil.save(signInDto.getEmail(), tokenDto.getRefreshToken());
            return tokenDto;
        } catch (Exception e) {
            log.error("로그인 실패", e);
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    // Bearer 제거 및 형식 검증
    public String resolveRefreshToken(String refreshToken) {
        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        return refreshToken.substring(7);
    }

    // 토큰 재발급 처리
    @Transactional
    public TokenDto reissue(String bearerToken) {
        String refreshToken = resolveRefreshToken(bearerToken);
        return tokenProvider.reissueAccessToken(refreshToken);
    }
}

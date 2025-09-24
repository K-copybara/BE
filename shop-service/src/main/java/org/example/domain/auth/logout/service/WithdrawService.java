package org.example.domain.auth.logout.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.auth.logout.repository.LogoutRepository;
import org.example.domain.config.jwt.TokenProvider;
import org.example.domain.config.redis.RedisUtil;
import org.example.domain.entity.Store;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawService {
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;
    private final LogoutRepository logoutRepository;

    @Transactional
    public void withdraw(String token) {
        // 토큰에서 상점 이메일 추출
        String email = tokenProvider.getEmailFromToken(token);
        long expiration = tokenProvider.getExpiration(token);

        // 토큰 무효화 (로그아웃과 동일)
        redisUtil.delete(email); // RefreshToken 삭제
        redisUtil.setBlackList(token, "logout", expiration); // AccessToken 블랙리스트 등록

        // Store 상태를 비활성화로 변경
        Store store = logoutRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("상점 정보를 찾을 수 없습니다."));
        store.deactivate(); // Store 엔티티에 상태 변경 메서드 추가 필요

        log.info("상점 탈퇴 처리 완료: {}", email);
    }

}

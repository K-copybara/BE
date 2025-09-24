package org.example.domain.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.example.domain.auth.login.dto.TokenDto;
import org.example.domain.auth.login.repository.StoreRepository;
import org.example.domain.config.redis.RedisUtil;
import org.example.domain.config.security.CustomUserDetails;
import org.example.domain.entity.Store;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24;            // 1일
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일
    private final Key key;
    private final RedisUtil redisUtil;
    private final StoreRepository storeRepository;

    public TokenProvider(@Value("${custom.jwt.secret}") String secretKey, RedisUtil redisUtil, StoreRepository storeRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisUtil = redisUtil;
        this.storeRepository = storeRepository;
    }

    public TokenDto generateTokenDto(Authentication authentication) {
        // 권한들 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String accessToken = generateAccessToken(authentication.getName(), authorities);
        String refreshToken = generateRefreshToken(authentication.getName(), authorities);

        long now = System.currentTimeMillis();

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(now + ACCESS_TOKEN_EXPIRE_TIME)
                .refreshToken(refreshToken)
                .build();
    }



    public TokenDto reissueAccessToken(String refreshToken) {

        // 리프레시 토큰에서 사용자 정보 추출 -> 클레임 확인
        Claims claims = parseClaims(refreshToken);

        // Refresh Token 유효성 + isRefreshToken 클레임 체크
        if (!validateToken(refreshToken)) {
            throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
        }

        Object refreshClaim = claims.get("isRefreshToken");
        if (!(refreshClaim instanceof Boolean) || !((Boolean) refreshClaim)) {
            throw new BadCredentialsException("이 토큰은 리프레시 토큰이 아닙니다.");
        }

        // 사용자 정보 추출
        String email = claims.getSubject();
        String authorities = claims.get(AUTHORITIES_KEY).toString();

        // Redis에서 저장된 토큰과 일치하는지 확인
        String storedToken = redisUtil.getData(email);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BadCredentialsException("리프레시 토큰이 일치하지 않습니다. 다시 로그인 해주세요.");
        }

        // 새 토큰 생성
        String newAccessToken = generateAccessToken(email, authorities);
        String newRefreshToken = generateRefreshToken(email, authorities);

        // Redis에 새 토큰 저장 - 기존 값 덮어쓰기
        redisUtil.save(email, newRefreshToken);

        // 반환
        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(newAccessToken)
                .accessTokenExpiresIn(new Date((new Date()).getTime() + ACCESS_TOKEN_EXPIRE_TIME).getTime())
                .refreshToken(newRefreshToken)
                .build();
    }

    private String generateAccessToken(String email, String authorities) {
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        return Jwts.builder()
                .setSubject(email)
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String generateRefreshToken(String email, String authorities) {
        long now = (new Date()).getTime();
        return Jwts.builder()
                .setSubject(email)
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .claim("isRefreshToken", true) // refreshToken 임을 나타내는 클레임 추가
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 토큰 subject = email
        String email = claims.getSubject();

        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("상점을 찾을 수 없습니다."));

        if (Boolean.FALSE.equals(store.getStatus())) {
            throw new RuntimeException("비활성화된 상점입니다.");
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(
                store.getId(),
                store.getEmail(),
                store.getPassword(),
                store.getShopName(),
                authorities
        );

        return new UsernamePasswordAuthenticationToken(customUserDetails, "", authorities);
    }


    public boolean validateToken(String token) {
        try {
            // 1. 블랙리스트 확인
            if (redisUtil.isBlackList(token)) {
                log.info("블랙리스트에 포함된 토큰입니다.");
                return false;
            }

            // 2. 서명/만료 검사
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }


    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
    // 토큰에서 이메일(subject)을 추출
    // Authentication 필요 없음
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    //accessToken의 만료시간을 추출
    // 로그아웃할 때 해당 accessToken을 블랙리스트에 저장할때 TTL로 설정해야함
    //  이건 "남은 만료 시간"을 계산하는 것이지, expiration.getTime()만 반환하면 고정된 시간값이라 TTL이 아님!
    public long getExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }



}

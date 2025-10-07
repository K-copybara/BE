package org.example.domain.config.security;

import lombok.RequiredArgsConstructor;
import org.example.domain.auth.login.repository.StoreRepository;
import org.example.domain.entity.Store;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email + " -> 데이터베이스에서 찾을 수 없습니다."));

        // 상태 확인 추가
        if (Boolean.FALSE.equals(store.getStatus())) {
            throw new UsernameNotFoundException(email + " -> 비활성화된 상점입니다.");
        }

        return createUserDetails(store);
    }

    // DB에 Store 값이 존재한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(Store store) {
        // 상점은 기본적으로 ROLE_STORE
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_STORE");

        return new CustomUserDetails(
                store.getId(),
                store.getEmail(),
                store.getPassword(),
                store.getShopName(),
                Collections.singleton(grantedAuthority)
        );
    }
}

package org.example.domain.config.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 인증 정보 없을 때 401 에러
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String message = "인증되지 않은 요청입니다. AccessToken이 유효하지 않거나 만료되었습니다.";

        response.getWriter().write(
                "{ \"status\": 401, \"error\": \"UNAUTHORIZED\", \"message\": \"" + message + "\" }"
        );
    }
}

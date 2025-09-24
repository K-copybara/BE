package org.example.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입되어 있는 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "잘못된 자격 증명입니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "상점을 찾을 수 없습니다."),
    STORE_DISABLED(HttpStatus.FORBIDDEN, "비활성화된 상점입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String message;
}
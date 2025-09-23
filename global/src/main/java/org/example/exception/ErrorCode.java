package org.example.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NICKNAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자 아이디입니다."),
    USER_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 이메일을 찾을 수 없습니다."),
    USER_DELETED(HttpStatus.FORBIDDEN, "탈퇴한 회원입니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "중복된 이메일입니다"),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "중복된 닉네임입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "일치하지 않는 패스워드입니다."),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "기존과 동일한 비밀번호입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "사용자 인증에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
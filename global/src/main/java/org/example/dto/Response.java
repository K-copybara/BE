package org.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class Response<T> {
    private final boolean success;
    private final String message;
    private final T data;

    private Response(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 성공 (데이터 포함)
    public static <T> Response<T> success(String message, T data) {
        return new Response<>(true, message, data);
    }

    // 성공 (데이터 없음)
    public static <T> Response<T> success(String message) {
        return new Response<>(true, message, null);
    }

    // 실패
    public static <T> Response<T> fail(String message) {
        return new Response<>(false, message, null);
    }
}

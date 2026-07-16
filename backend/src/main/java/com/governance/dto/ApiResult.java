package com.governance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;
    private String traceId;

    public ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(0, "success", data);
    }

    public static <T> ApiResult<T> ok() {
        return new ApiResult<>(0, "success", null);
    }

    public static <T> ApiResult<T> fail(String message) {
        return new ApiResult<>(1, message, null);
    }

    public static <T> ApiResult<T> fail(int code, String message) {
        return new ApiResult<>(code, message, null);
    }
}

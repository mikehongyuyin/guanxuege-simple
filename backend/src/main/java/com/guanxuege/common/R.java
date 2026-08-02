package com.guanxuege.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一接口返回结构
 */
@Data
public class R<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public static <T> R<T> ok(T data) {
        return of(0, "success", data);
    }

    public static <T> R<T> ok() {
        return of(0, "success", null);
    }

    public static <T> R<T> fail(String message) {
        return of(500, message, null);
    }

    public static <T> R<T> fail(int code, String message) {
        return of(code, message, null);
    }

    private static <T> R<T> of(int code, String message, T data) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        r.data = data;
        return r;
    }
}

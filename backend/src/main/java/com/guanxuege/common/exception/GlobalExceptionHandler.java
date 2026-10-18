package com.guanxuege.common.exception;

import com.guanxuege.common.R;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：统一把异常转成 R 结构
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBiz(BusinessException e) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(R.fail(e.getCode(), e.getMessage()));
    }

    /** 越权访问：资源不属于当前用户，返回 403 */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<R<Void>> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(R.fail(403, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handle(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.fail(e.getMessage() == null ? "系统异常" : e.getMessage()));
    }
}

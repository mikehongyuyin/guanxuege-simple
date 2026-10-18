package com.guanxuege.common.exception;

/**
 * 数据归属校验失败（越权访问）：
 * 资源存在但不属于当前登录用户，统一返回 HTTP 403。
 * <p>
 * 与 BusinessException 的区别：BusinessException 通常表示参数或业务状态问题，
 * 返回 HTTP 200 + 业务错误码；越权属于安全事件，返回 403 便于监控与告警。
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}

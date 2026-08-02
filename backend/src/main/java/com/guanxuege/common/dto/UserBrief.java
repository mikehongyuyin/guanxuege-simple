package com.guanxuege.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户简要信息（单体应用内由 auth 包直接提供，避免泄露敏感字段）
 */
@Data
public class UserBrief implements Serializable {
    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
}

package com.guanxuege.auth.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterDTO implements Serializable {
    private String phone;
    private String password;
    private String nickname;

    /** 是否已同意《用户协议》与《隐私政策》（注册强制，必须 true） */
    private Boolean privacyAgreed;

    /** 已阅读的《隐私政策》版本 */
    private String consentVersion;
}

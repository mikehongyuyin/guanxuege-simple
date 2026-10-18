package com.guanxuege.auth.dto;

import lombok.Data;

/**
 * 用户资料更新入参。
 * <p>
 * 只包含允许用户自助维护的字段：手机号（登录名）、角色、状态等敏感字段不接受修改，
 * 由服务端从当前登录态与库中记录取值，防止越权改号。
 */
@Data
public class UserProfileUpdateDTO {

    /** 账号昵称 */
    private String nickname;

    /** 社区昵称：留空表示清除（回落到账号昵称） */
    private String communityNickname;

    /** 微信号 */
    private String wechat;

    /** 邮箱 */
    private String email;

    /** 个性签名 */
    private String signature;
}

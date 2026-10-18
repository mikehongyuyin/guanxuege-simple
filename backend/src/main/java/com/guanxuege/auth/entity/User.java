package com.guanxuege.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 家长/管理员账户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_user")
public class User extends BaseEntity {

    /** 手机号（登录名） */
    private String phone;

    /** BCrypt 加密后的密码（不对外序列化，避免泄露哈希） */
    @JsonIgnore
    private String password;

    /** 账号昵称 */
    private String nickname;

    /** 社区昵称：设置后在社区内替代账号昵称展示 */
    private String communityNickname;

    /** 头像 URL */
    private String avatar;

    /** 微信号（仅本人可见，查看他人资料时不返回） */
    private String wechat;

    /** 邮箱（仅本人可见，查看他人资料时不返回） */
    private String email;

    /** 个性签名（公开展示） */
    private String signature;

    /** 角色：1 家长 / 2 管理员 */
    private Integer role;

    /** 状态：1 正常 / 0 禁用 */
    private Integer status;
}

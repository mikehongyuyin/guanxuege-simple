package com.guanxuege.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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

    /** BCrypt 加密后的密码 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 角色：1 家长 / 2 管理员 */
    private Integer role;

    /** 状态：1 正常 / 0 禁用 */
    private Integer status;
}

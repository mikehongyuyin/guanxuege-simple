package com.guanxuege.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户资料视图。
 * <p>
 * 隐私约定（重要）：手机号、微信号、邮箱属于联系方式，
 * **仅当查看者本人（isSelf = true）时返回**；他人查看时一律为 null，
 * 前端展示为「仅本人可见」。避免公开社区里被批量抓取联系方式。
 */
@Data
public class UserProfileVO {

    private Long id;

    /** 账号昵称 */
    private String nickname;

    /** 社区昵称（可为空） */
    private String communityNickname;

    /** 展示名：社区昵称优先，否则账号昵称 */
    private String displayName;

    private String avatar;

    /** 个性签名 */
    private String signature;

    /** 手机号：仅本人可见 */
    private String phone;

    /** 微信号：仅本人可见 */
    private String wechat;

    /** 邮箱：仅本人可见 */
    private String email;

    /** 发帖数 */
    private Integer postCount;

    /** 评论数 */
    private Integer commentCount;

    private LocalDateTime createTime;

    /** 查看者是否为本人 */
    private Boolean self;
}

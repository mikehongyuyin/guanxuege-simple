package com.guanxuege.community.vo;

import lombok.Data;

/**
 * @提及的轻量视图：前端据此把正文里的「@昵称」渲染成可点击的链接。
 */
@Data
public class UserMentionVO {
    private Long userId;

    /** 被 @ 时命中的展示名（社区昵称优先） */
    private String nickname;
}

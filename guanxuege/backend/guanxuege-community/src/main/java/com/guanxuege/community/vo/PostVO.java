package com.guanxuege.community.vo;

import com.guanxuege.community.entity.Post;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子视图对象：在 Post 基础上补齐作者信息（经 Feign 从 auth 获取）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostVO extends Post {
    private String authorNickname;
    private String authorAvatar;
}

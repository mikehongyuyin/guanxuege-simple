package com.guanxuege.community.vo;

import com.guanxuege.community.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 评论视图对象：在 Comment 基础上补齐作者昵称/头像与 @ 提及，供前端展示
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends Comment {
    private String authorNickname;
    private String authorAvatar;

    /** 评论中 @ 到的用户 */
    private List<UserMentionVO> mentions;
}

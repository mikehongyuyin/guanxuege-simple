package com.guanxuege.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子评论（parent_id 非空表示对某条评论的回复）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_comment")
public class Comment extends BaseEntity {

    /** 帖子 id */
    private Long postId;

    /** 评论用户 id */
    private Long userId;

    /** 回复的父评论 id */
    private Long parentId;

    /** 评论内容 */
    private String content;

    /** 评论图片（JSON 数组，如 ["/api/images/xx.jpg"]） */
    private String images;

    /** 命中焦虑词标记：1 是 / 0 否 */
    private Integer anxietyFlag;

    /** 点赞数 */
    private Integer likeCount;
}

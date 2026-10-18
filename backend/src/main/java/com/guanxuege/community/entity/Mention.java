package com.guanxuege.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @提及关系：帖子 / 评论中 @ 到某个用户。
 * <p>
 * 帖子与评论共用一张表，用 biz_type 区分；uk_biz_user 保证同一内容不会重复记录同一个人。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_mention")
public class Mention extends BaseEntity {

    /** 业务类型：post / comment */
    private String bizType;

    /** 业务 id（帖子 id 或评论 id） */
    private Long bizId;

    /** 被 @ 的用户 id */
    private Long userId;
}

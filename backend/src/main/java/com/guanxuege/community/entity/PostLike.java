package com.guanxuege.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子点赞关系表
 * <p>
 * 用于保证点赞幂等：同一用户对同一帖子最多存在一条记录，
 * 记录存在即视为已点赞。点赞表不使用逻辑删除，
 * 取消点赞直接物理删除，避免高频 toggle 产生大量墓碑数据。
 */
@Data
@TableName("gx_post_like")
public class PostLike implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 帖子 id */
    private Long postId;

    /** 点赞用户 id */
    private Long userId;

    private LocalDateTime createTime;
}

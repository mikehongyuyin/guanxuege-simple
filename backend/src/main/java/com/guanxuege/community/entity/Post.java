package com.guanxuege.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区帖子
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_post")
public class Post extends BaseEntity {

    /** 作者用户 id */
    private Long authorId;

    /** 标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 标签，如「习惯养成」「成绩交流」 */
    private String tag;

    /** 浏览数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 热度分（用于热门排序） */
    private Integer hotScore;

    /** 状态：1 正常 / 0 下架 */
    private Integer status;
}

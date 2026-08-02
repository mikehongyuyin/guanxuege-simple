package com.guanxuege.community.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 热议话题
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_topic")
public class Topic extends BaseEntity {

    /** 话题名称 */
    private String name;

    /** 讨论数 */
    private Integer discussCount;

    /** 排序权重 */
    @TableField("`rank`")
    private Integer rank;
}

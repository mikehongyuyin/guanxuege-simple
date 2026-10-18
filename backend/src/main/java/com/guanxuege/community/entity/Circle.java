package com.guanxuege.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区圈子（分层圈子，用户手动加入）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_circle")
public class Circle extends BaseEntity {

    /** 圈子名称 */
    private String name;

    /** 所属社区：parenting 育儿 / voice 心声 */
    private String community;

    /** 适用阶段，如「一至三年级」 */
    private String scope;

    /** 圈子简介 */
    private String description;

    /** 标识色 */
    private String color;

    /** 成员数 */
    private Integer memberCount;

    /** 排序权重 */
    private Integer sort;
}

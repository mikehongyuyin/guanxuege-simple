package com.guanxuege.learningplan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学习计划
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_plan")
public class Plan extends BaseEntity {

    /** 关联孩子 id */
    private Long childId;

    /** 计划标题 */
    private String title;

    /** 计划说明 */
    private String description;

    /** 计划类型：DAY 日计划 / WEEK 周计划 / MONTH 月计划 */
    private String planType;

    /** 状态：0 待开始 / 1 进行中 / 2 已完成 */
    private Integer status;

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 整体进度 0-100 */
    private Integer progress;
}

package com.guanxuege.learningplan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 计划任务（学习计划下的具体事项）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_plan_task")
public class PlanTask extends BaseEntity {

    /** 所属计划 id */
    private Long planId;

    /** 任务标题 */
    private String title;

    /** 科目，如「数学」「语文」 */
    private String subject;

    /** 状态：0 待开始 / 1 进行中 / 2 已完成 */
    private Integer status;

    /** 截止日期 */
    private LocalDate dueDate;

    /** 进度 0-100 */
    private Integer progress;

    /** 备注 */
    private String remark;
}

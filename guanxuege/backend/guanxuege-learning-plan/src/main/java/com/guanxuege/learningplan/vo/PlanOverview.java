package com.guanxuege.learningplan.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlanOverview implements Serializable {
    /** 待开始计划数 */
    private Long todoCount;
    /** 进行中计划数 */
    private Long doingCount;
    /** 已完成计划数 */
    private Long doneCount;
    /** 任务总数 */
    private Long taskTotal;
    /** 已完成任务数 */
    private Long taskDone;
    /** 整体进度 0-100 */
    private Integer overallProgress;
}

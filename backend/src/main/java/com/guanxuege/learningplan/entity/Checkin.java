package com.guanxuege.learningplan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 任务打卡记录（同一任务同一天仅一条）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_checkin")
public class Checkin extends BaseEntity {

    /** 计划任务 id */
    private Long taskId;

    /** 孩子 id */
    private Long childId;

    /** 打卡日期 */
    private LocalDate checkinDate;

    /** 打卡记录 */
    private String note;
}

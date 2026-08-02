package com.guanxuege.grade.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 成绩记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_grade")
public class Grade extends BaseEntity {

    /** 关联孩子 id */
    private Long childId;

    /** 科目，如「语文」「数学」「英语」「科学」 */
    private String subject;

    /** 考试名称 */
    private String examName;

    /** 得分 */
    private java.math.BigDecimal score;

    /** 满分 */
    private java.math.BigDecimal fullScore;

    /** 考试日期 */
    private LocalDate examDate;

    /** 班级排名 */
    private Integer rankInClass;

    /** 教师/家长评语 */
    private String comment;
}

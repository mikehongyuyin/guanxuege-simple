package com.guanxuege.grade.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SubjectStat implements Serializable {
    private String subject;
    /** 平均分 */
    private BigDecimal average;
    /** 满分 */
    private BigDecimal fullScore;
    /** 得分率 0-100 */
    private Integer rate;
    /** 记录数 */
    private Long count;
}

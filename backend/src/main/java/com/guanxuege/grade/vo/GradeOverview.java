package com.guanxuege.grade.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GradeOverview implements Serializable {
    /** 总分平均（各科平均分的平均） */
    private BigDecimal overallAverage;
    /** 各科统计 */
    private List<SubjectStat> subjectStats;
}

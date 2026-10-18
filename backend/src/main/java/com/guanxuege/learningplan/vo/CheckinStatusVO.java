package com.guanxuege.learningplan.vo;

import lombok.Data;

/**
 * 打卡状态视图
 */
@Data
public class CheckinStatusVO {

    /** 今日是否已打卡 */
    private Boolean checkedToday;

    /** 连续打卡天数 */
    private Integer streak;

    /** 累计打卡次数 */
    private Integer totalCount;
}

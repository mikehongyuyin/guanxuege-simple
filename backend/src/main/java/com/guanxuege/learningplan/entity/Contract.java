package com.guanxuege.learningplan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 家庭契约：孩子与家长双向承诺 + 奖励与违约约定
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_contract")
public class Contract extends BaseEntity {

    /** 家长 id */
    private Long parentId;

    /** 孩子 id */
    private Long childId;

    /** 契约标题 */
    private String title;

    /** 孩子的承诺 */
    private String childPromise;

    /** 家长的承诺 */
    private String parentPromise;

    /** 达成奖励 */
    private String reward;

    /** 违约约定 */
    private String penalty;

    /** 状态：0 进行中 / 1 已完成 / 2 已违约 */
    private Integer status;

    /** 孩子已确认：1 是 / 0 否 */
    private Integer childConfirm;

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;
}

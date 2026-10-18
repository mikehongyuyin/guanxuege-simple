package com.guanxuege.community.vo;

import com.guanxuege.community.entity.Circle;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 圈子视图：Circle + 当前用户是否已加入
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CircleVO extends Circle {
    private Boolean joined;
}

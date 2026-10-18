package com.guanxuege.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户圈子关系（手动加入 / 退出）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_user_circle")
public class UserCircle extends BaseEntity {

    /** 用户 id */
    private Long userId;

    /** 圈子 id */
    private Long circleId;
}

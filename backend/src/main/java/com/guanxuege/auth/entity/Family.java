package com.guanxuege.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 家庭档案（一个家长一份）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_family")
public class Family extends BaseEntity {

    /** 所属家长 id */
    private Long parentId;

    /** 家庭昵称 */
    private String familyName;

    /** 家庭类型：核心家庭 / 二胎家庭 / 单亲家庭 / 隔代养育 / 其他 */
    private String familyType;

    /** 家风宣言 */
    private String motto;
}

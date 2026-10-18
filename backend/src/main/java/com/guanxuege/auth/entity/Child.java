package com.guanxuege.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 孩子档案（一个家长可关联多个孩子）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_child")
public class Child extends BaseEntity {

    /** 所属家长 id */
    private Long parentId;

    /** 姓名 */
    private String name;

    /** 年级，如「二年级」 */
    private String grade;

    /** 学校 */
    private String school;

    /** 头像 */
    private String avatar;

    /** 出生日期 */
    private LocalDate birthday;

    /** 性别 1 男 / 2 女 */
    private Integer gender;

    /** 性格特点（逗号分隔标签） */
    private String personality;

    /** 兴趣特长（逗号分隔标签） */
    private String interests;
}

package com.guanxuege.auth.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConsentDTO implements Serializable {
    /** 关联孩子 id（注册类同意可不传） */
    private Long childId;

    /** 同意类型：USER_REGISTER / CHILD_INFO */
    private String consentType;

    /** 同意的文档版本 */
    private String version;

    /** 监护人关系声明（CHILD_INFO 必填） */
    private String guardianRelation;
}

package com.guanxuege.auth.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 绑定孩子请求体：在原有孩子档案字段之外，
 * 增加「监护人关系声明 + 儿童个人信息专项同意」字段，用于服务端强制校验。
 */
@Data
public class ChildCreateDTO implements Serializable {

    private String name;
    private String grade;
    private String school;
    private String avatar;
    private LocalDate birthday;
    private Integer gender;
    private String personality;
    private String interests;

    /** 监护人关系声明：父母 / 祖父母 / 其他监护人（必填） */
    private String guardianRelation;

    /** 已阅读的《儿童个人信息保护规则》版本 */
    private String consentVersion;

    /** 是否已同意《儿童个人信息保护规则》专项同意（必须 true） */
    private Boolean consentAgreed;
}

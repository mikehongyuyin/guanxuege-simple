package com.guanxuege.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanxuege.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户同意记录（含儿童个人信息专项同意）
 * <p>采用「追加 + 状态位」方式保留审计轨迹：撤回时仅将 agreed 置 0 并记录撤回时间，不物理删除。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gx_consent_record")
public class ConsentRecord extends BaseEntity {

    /** 家长 id */
    private Long parentId;

    /** 关联孩子 id（注册类同意为 null，儿童信息类同意关联具体孩子） */
    private Long childId;

    /** 同意类型：USER_REGISTER / CHILD_INFO */
    private String consentType;

    /** 同意的文档版本，如 1.0 */
    private String version;

    /** 监护人关系声明：父母 / 祖父母 / 其他监护人 */
    private String guardianRelation;

    /** 1 已同意 / 0 已撤回 */
    private Integer agreed;

    /** 同意时间 */
    private LocalDateTime agreedAt;

    /** 撤回时间 */
    private LocalDateTime withdrawnAt;
}

package com.guanxuege.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户简要信息（单体应用内由 auth 包直接提供，避免泄露敏感字段）
 * <p>
 * 注意：这里只有公开字段，微信号 / 邮箱等联系方式不在其中。
 */
@Data
public class UserBrief implements Serializable {
    private Long id;
    private String nickname;

    /** 社区昵称（可能为空） */
    private String communityNickname;

    /**
     * 社区展示名：有社区昵称时优先，否则回落账号昵称。
     * 业务方应统一用这个字段展示，避免出现"两处昵称不一致"。
     */
    private String displayName;

    private String avatar;
}

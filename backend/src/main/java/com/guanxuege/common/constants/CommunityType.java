package com.guanxuege.common.constants;

/**
 * 社区类型。
 * <p>
 * 帖子与圈子共用同一套表，靠 {@code community} 字段区分归属社区，
 * 避免为新社区复制整套表结构与业务逻辑（点赞幂等、内容护栏、@提及只写一份）。
 */
public final class CommunityType {

    /** 育儿社区（默认） */
    public static final String PARENTING = "parenting";

    /** 心声社区：工作 / 副业 / 创业 / 生活 */
    public static final String VOICE = "voice";

    private CommunityType() {
    }

    /**
     * 归一化：空值或非法值一律回退为育儿社区，避免脏数据导致帖子"消失"。
     */
    public static String normalize(String community) {
        if (community == null || community.isBlank()) {
            return PARENTING;
        }
        String v = community.trim();
        return VOICE.equalsIgnoreCase(v) ? VOICE : PARENTING;
    }
}

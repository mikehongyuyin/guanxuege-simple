package com.guanxuege.common.constants;

/**
 * @提及的业务类型：帖子与评论共用一张关系表，用 biz_type 区分。
 */
public final class MentionBizType {

    public static final String POST = "post";

    public static final String COMMENT = "comment";

    private MentionBizType() {
    }
}

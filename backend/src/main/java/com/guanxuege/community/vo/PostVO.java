package com.guanxuege.community.vo;

import com.guanxuege.community.entity.Post;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 帖子视图对象：在 Post 基础上补齐作者信息（单体应用内直接调用 auth 获取）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostVO extends Post {
    private String authorNickname;
    private String authorAvatar;

    /** 圈子名称（冗余展示用） */
    private String circleName;

    /** 反焦虑护栏提示文案（命中焦虑词时返回，不落库） */
    private String warning;

    /**
     * 当前登录用户是否已点赞该帖子。
     * 用于前端正确展示「已赞」状态并支持取消点赞，避免重复计数带来的状态错乱。
     * 未登录或不适用时为 false。
     */
    private Boolean liked;

    /**
     * 正文中 @ 到的用户（昵称 + id），供前端高亮并支持点击查看资料。
     * 只包含真实存在的用户；@ 了一个不存在的昵称不会产生记录。
     */
    private List<UserMentionVO> mentions;
}

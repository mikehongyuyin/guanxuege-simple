package com.guanxuege.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.vo.PostVO;

import java.util.List;

public interface PostService extends IService<Post> {

    /**
     * 按标签分页查询：recommend 推荐 / latest 最新 / hot 热议；circleId 不为空时按圈子过滤
     *
     * @param currentUserId 当前登录用户 id（可为 null），用于回填每条帖子的 liked 状态
     */
    List<PostVO> listByTab(String tab, int page, int size, Long circleId, Long currentUserId);

    /**
     * 带社区维度的分页查询。育儿社区与心声社区共用一张表，靠 community 字段隔离。
     */
    List<PostVO> listByTab(String tab, int page, int size, Long circleId, Long currentUserId, String community);

    /** 发帖：落库（单体应用内同步完成，不再投递 RocketMQ 事件） */
    PostVO create(Post post, Long authorId);

    /**
     * 帖子详情
     *
     * @param currentUserId 当前登录用户 id（可为 null），用于回填 liked 状态
     */
    PostVO getVo(Long id, Long currentUserId);

    /** 编辑帖子（仅作者本人） */
    PostVO updatePost(Long id, Post post, Long userId);

    /** 删除帖子（仅作者本人） */
    Boolean deletePost(Long id, Long userId);

    /**
     * 点赞：同一用户重复调用视为取消点赞（toggle），保证幂等不重复计数。
     *
     * @param id     帖子 id
     * @param userId 当前登录用户 id
     * @return 最新点赞数
     */
    Integer like(Long id, Long userId);
}

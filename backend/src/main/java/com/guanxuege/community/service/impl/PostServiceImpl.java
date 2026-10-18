package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.constants.CommunityType;
import com.guanxuege.common.constants.MentionBizType;
import com.guanxuege.common.dto.UserBrief;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.exception.ForbiddenException;
import com.guanxuege.common.util.WordFilterUtil;
import com.guanxuege.community.entity.Circle;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.entity.PostLike;
import com.guanxuege.community.mapper.CircleMapper;
import com.guanxuege.community.mapper.PostLikeMapper;
import com.guanxuege.community.mapper.PostMapper;
import com.guanxuege.community.service.MentionService;
import com.guanxuege.community.service.PostService;
import com.guanxuege.community.vo.PostVO;
import com.guanxuege.community.vo.UserMentionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private UserService userService;

    @Autowired
    private CircleMapper circleMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private MentionService mentionService;

    @Override
    public List<PostVO> listByTab(String tab, int page, int size, Long circleId, Long currentUserId) {
        return listByTab(tab, page, size, circleId, currentUserId, CommunityType.PARENTING);
    }

    @Override
    public List<PostVO> listByTab(String tab, int page, int size, Long circleId, Long currentUserId, String community) {
        String type = CommunityType.normalize(community);
        var query = Wrappers.<Post>lambdaQuery()
                .eq(Post::getStatus, 1)
                .eq(Post::getCommunity, type);
        if (circleId != null && circleId > 0) {
            query.eq(Post::getCircleId, circleId);
        }
        if ("latest".equals(tab)) {
            query.orderByDesc(Post::getCreateTime);
        } else {
            // recommend / hot 均按热度
            query.orderByDesc(Post::getHotScore);
        }
        Page<Post> p = baseMapper.selectPage(new Page<>(page, size), query);
        return toVoList(p.getRecords(), currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostVO create(Post post, Long authorId) {
        // 审核与反焦虑护栏：违禁词拦截，焦虑词标记并温和提醒
        String combined = (post.getTitle() == null ? "" : post.getTitle())
                + (post.getContent() == null ? "" : post.getContent());
        WordFilterUtil.assertSafe(combined);
        String warning = WordFilterUtil.findAnxietyWarning(combined);

        post.setAuthorId(authorId);
        post.setCommunity(CommunityType.normalize(post.getCommunity()));
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setHotScore(1);
        post.setStatus(1);
        post.setAnxietyFlag(warning != null ? 1 : 0);
        baseMapper.insert(post);

        // 解析正文里的 @昵称，落库为提及关系
        mentionService.save(MentionBizType.POST, post.getId(), post.getContent());

        PostVO vo = getVo(post.getId(), authorId);
        vo.setWarning(warning);
        return vo;
    }

    @Override
    public PostVO getVo(Long id, Long currentUserId) {
        Post post = baseMapper.selectById(id);
        return post == null ? null : enrich(post, currentUserId);
    }

    /**
     * 点赞：幂等 toggle。
     * 已点赞则取消并回退计数，未点赞则新增记录并累加计数。
     * 同一用户对同一帖重复调用不会产生无限累加。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer like(Long id, Long userId) {
        Post post = baseMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        int likeCount = post.getLikeCount() == null ? 0 : post.getLikeCount();

        Long existId = postLikeMapper.findRawId(id, userId);
        boolean cancel;
        if (existId != null) {
            // 已点赞 -> 取消点赞
            postLikeMapper.deleteById(existId);
            cancel = true;
        } else {
            // 未点赞 -> 新增点赞关系（uk_post_user 唯一约束兜底幂等）
            PostLike postLike = new PostLike();
            postLike.setPostId(id);
            postLike.setUserId(userId);
            postLikeMapper.insert(postLike);
            cancel = false;
        }
        // 计数走数据库原子自增 / 自减，避免「读-改-写」在并发下丢更新
        if (cancel) {
            baseMapper.update(null, Wrappers.<Post>lambdaUpdate()
                    .setSql("like_count = like_count - 1, hot_score = hot_score - 1")
                    .eq(Post::getId, id)
                    .gt(Post::getLikeCount, 0));
        } else {
            baseMapper.update(null, Wrappers.<Post>lambdaUpdate()
                    .setSql("like_count = like_count + 1, hot_score = hot_score + 1")
                    .eq(Post::getId, id));
        }
        Post fresh = baseMapper.selectById(id);
        return fresh == null ? likeCount : fresh.getLikeCount();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostVO updatePost(Long id, Post post, Long userId) {
        Post exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("帖子不存在");
        }
        if (!exist.getAuthorId().equals(userId)) {
            // 越权属安全事件，抛 403 便于监控告警
            throw new ForbiddenException("只能编辑自己发布的帖子");
        }
        String combined = (post.getTitle() == null ? "" : post.getTitle())
                + (post.getContent() == null ? "" : post.getContent());
        WordFilterUtil.assertSafe(combined);
        String warning = WordFilterUtil.findAnxietyWarning(combined);

        exist.setTitle(post.getTitle());
        exist.setTag(post.getTag());
        exist.setCircleId(post.getCircleId());
        exist.setContent(post.getContent());
        exist.setAnxietyFlag(warning != null ? 1 : 0);
        baseMapper.updateById(exist);

        // 正文变化后同步刷新 @提及关系
        mentionService.save(MentionBizType.POST, id, exist.getContent());

        PostVO vo = getVo(id, userId);
        vo.setWarning(warning);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePost(Long id, Long userId) {
        Post exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("帖子不存在");
        }
        if (!exist.getAuthorId().equals(userId)) {
            // 越权属安全事件，抛 403 便于监控告警
            throw new ForbiddenException("只能删除自己发布的帖子");
        }
        mentionService.remove(MentionBizType.POST, id);
        return baseMapper.deleteById(id) > 0;
    }

    private List<PostVO> toVoList(List<Post> posts, Long currentUserId) {
        if (posts.isEmpty()) {
            return List.of();
        }
        // 批量取作者（按 id 去重，单体应用直接调用本地 UserService，无需 Feign）
        Map<Long, UserBrief> briefMap = new LinkedHashMap<>();
        for (Post p : posts) {
            if (!briefMap.containsKey(p.getAuthorId())) {
                try {
                    UserBrief brief = userService.getBrief(p.getAuthorId());
                    if (brief != null) {
                        briefMap.put(p.getAuthorId(), brief);
                    }
                } catch (Exception ignored) {
                    // 取作者失败时降级：仅展示帖子，不带作者信息
                }
            }
        }
        // 一次性查出当前用户在本页帖子上的点赞记录，避免逐条查询
        Set<Long> likedPostIds = likedPostIds(posts, currentUserId);
        // 一次性查出本页帖子的 @提及，避免 N+1
        Map<Long, List<UserMentionVO>> mentionMap =
                mentionService.mapByBizIds(MentionBizType.POST, posts.stream().map(Post::getId).toList());

        return posts.stream().map(p -> {
            PostVO vo = enrich(p, likedPostIds.contains(p.getId()));
            UserBrief brief = briefMap.get(p.getAuthorId());
            if (brief != null) {
                vo.setAuthorNickname(displayName(brief));
                vo.setAuthorAvatar(brief.getAvatar());
            }
            vo.setMentions(mentionMap.getOrDefault(p.getId(), List.of()));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 展示名：社区昵称优先，回落账号昵称 */
    private String displayName(UserBrief brief) {
        if (brief.getDisplayName() != null && !brief.getDisplayName().isBlank()) {
            return brief.getDisplayName();
        }
        return brief.getNickname();
    }

    /** 批量查询当前用户已点赞的帖子 id 集合 */
    private Set<Long> likedPostIds(List<Post> posts, Long currentUserId) {
        if (currentUserId == null || posts.isEmpty()) {
            return Set.of();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<PostLike> likes = postLikeMapper.selectList(
                Wrappers.<PostLike>lambdaQuery()
                        .eq(PostLike::getUserId, currentUserId)
                        .in(PostLike::getPostId, postIds));
        Set<Long> ids = new HashSet<>();
        for (PostLike like : likes) {
            ids.add(like.getPostId());
        }
        return ids;
    }

    private PostVO enrich(Post post, Long currentUserId) {
        boolean liked = currentUserId != null && postLikeMapper.findRawId(post.getId(), currentUserId) != null;
        return enrich(post, liked);
    }

    private PostVO enrich(Post post, boolean liked) {
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        vo.setLiked(liked);
        if (post.getCircleId() != null) {
            Circle circle = circleMapper.selectById(post.getCircleId());
            if (circle != null) {
                vo.setCircleName(circle.getName());
            }
        }
        vo.setMentions(listMentions(post.getId()));
        return vo;
    }

    private List<UserMentionVO> listMentions(Long postId) {
        List<UserMentionVO> mentions = mentionService.list(MentionBizType.POST, postId);
        return mentions == null ? new ArrayList<>() : mentions;
    }
}

package com.guanxuege.community.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.R;
import com.guanxuege.common.constants.MentionBizType;
import com.guanxuege.common.dto.UserBrief;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.exception.ForbiddenException;
import com.guanxuege.common.util.WordFilterUtil;
import com.guanxuege.community.entity.Comment;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.mapper.CommentMapper;
import com.guanxuege.community.mapper.PostMapper;
import com.guanxuege.community.service.MentionService;
import com.guanxuege.community.vo.CommentVO;
import com.guanxuege.community.vo.UserMentionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private MentionService mentionService;

    @GetMapping("/list")
    public R<List<CommentVO>> list(@RequestParam Long postId) {
        List<Comment> comments = commentMapper.selectList(
                Wrappers.<Comment>lambdaQuery().eq(Comment::getPostId, postId)
                        .orderByAsc(Comment::getCreateTime));
        // 批量取评论作者昵称/头像
        Map<Long, UserBrief> briefMap = new LinkedHashMap<>();
        for (Comment c : comments) {
            if (!briefMap.containsKey(c.getUserId())) {
                try {
                    UserBrief brief = userService.getBrief(c.getUserId());
                    if (brief != null) {
                        briefMap.put(c.getUserId(), brief);
                    }
                } catch (Exception ignored) {
                    // 取作者失败时降级：仅展示评论内容
                }
            }
        }
        // 一次性查出本页评论的 @提及，避免逐条查询
        Map<Long, List<UserMentionVO>> mentionMap =
                mentionService.mapByBizIds(MentionBizType.COMMENT,
                        comments.stream().map(Comment::getId).toList());

        return R.ok(comments.stream().map(c -> {
            CommentVO vo = new CommentVO();
            org.springframework.beans.BeanUtils.copyProperties(c, vo);
            UserBrief brief = briefMap.get(c.getUserId());
            if (brief != null) {
                vo.setAuthorNickname(brief.getDisplayName() != null && !brief.getDisplayName().isBlank()
                        ? brief.getDisplayName() : brief.getNickname());
                vo.setAuthorAvatar(brief.getAvatar());
            }
            vo.setMentions(mentionMap.getOrDefault(c.getId(), List.of()));
            return vo;
        }).collect(Collectors.toList()));
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R<Comment> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                             @RequestBody Comment comment) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        // 审核与反焦虑护栏：违禁词拦截，焦虑词标记
        WordFilterUtil.assertSafe(comment.getContent());
        comment.setAnxietyFlag(WordFilterUtil.findAnxietyWarning(comment.getContent()) != null ? 1 : 0);
        comment.setUserId(userId);
        comment.setLikeCount(0);
        commentMapper.insert(comment);

        // 解析评论里的 @昵称
        mentionService.save(MentionBizType.COMMENT, comment.getId(), comment.getContent());

        // 同步帖子评论数
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount((post.getCommentCount() == null ? 0 : post.getCommentCount()) + 1);
            postMapper.updateById(post);
        }
        return R.ok(comment);
    }

    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> delete(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                             @PathVariable("id") Long id) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            // 越权属安全事件，抛 403 便于监控告警
            throw new ForbiddenException("只能删除自己的评论");
        }
        mentionService.remove(MentionBizType.COMMENT, id);
        commentMapper.deleteById(id);

        // 同步帖子评论数
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            int count = Math.max(0, (post.getCommentCount() == null ? 0 : post.getCommentCount()) - 1);
            post.setCommentCount(count);
            postMapper.updateById(post);
        }
        return R.ok(true);
    }
}

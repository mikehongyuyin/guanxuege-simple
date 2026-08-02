package com.guanxuege.community.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.R;
import com.guanxuege.common.dto.UserBrief;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.community.entity.Comment;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.mapper.CommentMapper;
import com.guanxuege.community.mapper.PostMapper;
import com.guanxuege.community.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
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
        return R.ok(comments.stream().map(c -> {
            CommentVO vo = new CommentVO();
            org.springframework.beans.BeanUtils.copyProperties(c, vo);
            UserBrief brief = briefMap.get(c.getUserId());
            if (brief != null) {
                vo.setAuthorNickname(brief.getNickname());
                vo.setAuthorAvatar(brief.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList()));
    }

    @PostMapping
    public R<Comment> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                             @RequestBody Comment comment) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        comment.setUserId(userId);
        comment.setLikeCount(0);
        commentMapper.insert(comment);

        // 同步帖子评论数
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount((post.getCommentCount() == null ? 0 : post.getCommentCount()) + 1);
            postMapper.updateById(post);
        }
        return R.ok(comment);
    }

    @DeleteMapping("/{id}")
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
            throw new BusinessException("只能删除自己的评论");
        }
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

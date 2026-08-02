package com.guanxuege.community.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.community.entity.Comment;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.mapper.CommentMapper;
import com.guanxuege.community.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @GetMapping("/list")
    public R<List<Comment>> list(@RequestParam Long postId) {
        return R.ok(commentMapper.selectList(
                Wrappers.<Comment>lambdaQuery().eq(Comment::getPostId, postId)
                        .orderByAsc(Comment::getCreateTime)));
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
}

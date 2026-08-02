package com.guanxuege.community.controller;

import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.service.PostService;
import com.guanxuege.community.vo.PostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/list")
    public R<List<PostVO>> list(@RequestParam(defaultValue = "recommend") String tab,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        return R.ok(postService.listByTab(tab, page, size));
    }

    @PostMapping
    public R<PostVO> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                            @RequestBody Post post) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(postService.create(post, userId));
    }

    @GetMapping("/{id}")
    public R<PostVO> detail(@PathVariable("id") Long id) {
        return R.ok(postService.getVo(id));
    }

    @PostMapping("/{id}/like")
    public R<Integer> like(@PathVariable("id") Long id) {
        return R.ok(postService.like(id));
    }

    @PutMapping("/{id}")
    public R<PostVO> update(@PathVariable("id") Long id,
                            @RequestHeader(value = "X-User-Id", required = false) Long userId,
                            @RequestBody Post post) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(postService.updatePost(id, post, userId));
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id,
                             @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(postService.deletePost(id, userId));
    }
}

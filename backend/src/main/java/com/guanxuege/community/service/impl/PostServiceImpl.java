package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.dto.UserBrief;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.mapper.PostMapper;
import com.guanxuege.community.service.PostService;
import com.guanxuege.community.vo.PostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private UserService userService;

    @Override
    public List<PostVO> listByTab(String tab, int page, int size) {
        var query = Wrappers.<Post>lambdaQuery().eq(Post::getStatus, 1);
        if ("latest".equals(tab)) {
            query.orderByDesc(Post::getCreateTime);
        } else {
            // recommend / hot 均按热度
            query.orderByDesc(Post::getHotScore);
        }
        Page<Post> p = baseMapper.selectPage(new Page<>(page, size), query);
        return toVoList(p.getRecords());
    }

    @Override
    public PostVO create(Post post, Long authorId) {
        post.setAuthorId(authorId);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setHotScore(1);
        post.setStatus(1);
        baseMapper.insert(post);

        return getVo(post.getId());
    }

    @Override
    public PostVO getVo(Long id) {
        Post post = baseMapper.selectById(id);
        return post == null ? null : enrich(post);
    }

    @Override
    public Integer like(Long id) {
        Post post = baseMapper.selectById(id);
        if (post == null) {
            return null;
        }
        post.setLikeCount((post.getLikeCount() == null ? 0 : post.getLikeCount()) + 1);
        post.setHotScore((post.getHotScore() == null ? 0 : post.getHotScore()) + 1);
        baseMapper.updateById(post);
        return post.getLikeCount();
    }

    @Override
    public PostVO updatePost(Long id, Post post, Long userId) {
        Post exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("帖子不存在");
        }
        if (!exist.getAuthorId().equals(userId)) {
            throw new BusinessException("只能编辑自己发布的帖子");
        }
        exist.setTitle(post.getTitle());
        exist.setTag(post.getTag());
        exist.setContent(post.getContent());
        baseMapper.updateById(exist);
        return getVo(id);
    }

    @Override
    public Boolean deletePost(Long id, Long userId) {
        Post exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("帖子不存在");
        }
        if (!exist.getAuthorId().equals(userId)) {
            throw new BusinessException("只能删除自己发布的帖子");
        }
        return baseMapper.deleteById(id) > 0;
    }

    private List<PostVO> toVoList(List<Post> posts) {
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
        return posts.stream().map(p -> {
            PostVO vo = enrich(p);
            UserBrief brief = briefMap.get(p.getAuthorId());
            if (brief != null) {
                vo.setAuthorNickname(brief.getNickname());
                vo.setAuthorAvatar(brief.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private PostVO enrich(Post post) {
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }
}

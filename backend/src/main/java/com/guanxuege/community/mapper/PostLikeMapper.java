package com.guanxuege.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guanxuege.community.entity.PostLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PostLikeMapper extends BaseMapper<PostLike> {

    /** 查询某用户对某帖子的点赞记录 id（物理查询，不做逻辑删除过滤） */
    @Select("SELECT id FROM gx_post_like WHERE post_id = #{postId} AND user_id = #{userId} LIMIT 1")
    Long findRawId(@Param("postId") Long postId, @Param("userId") Long userId);
}

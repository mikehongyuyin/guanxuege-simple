package com.guanxuege.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guanxuege.community.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 热议话题：按 tag 实时统计帖子数 + 评论数。
     * community 非空时只统计该社区——否则心声社区会看到育儿话题，反之亦然。
     */
    @Select("SELECT p.tag AS tag, COUNT(DISTINCT p.id) AS postCount, COUNT(c.id) AS commentCount " +
            "FROM gx_post p LEFT JOIN gx_comment c ON c.post_id = p.id AND c.deleted = 0 " +
            "WHERE p.deleted = 0 AND p.tag IS NOT NULL AND p.tag != '' " +
            "  AND (#{community} IS NULL OR p.community = #{community}) " +
            "GROUP BY p.tag " +
            "ORDER BY (COUNT(DISTINCT p.id) + COUNT(c.id)) DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectHotTags(@Param("limit") int limit,
                                            @Param("community") String community);
}

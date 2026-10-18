package com.guanxuege.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guanxuege.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 按社区昵称或账号昵称精确匹配（用于 @提及解析），最多 5 条 */
    @Select("SELECT * FROM gx_user " +
            "WHERE deleted = 0 AND (community_nickname = #{name} OR nickname = #{name}) " +
            "LIMIT 5")
    List<User> selectByDisplayNameOrNickname(@Param("name") String name);

    /** 昵称关键字搜索：供 @提及选择器使用 */
    @Select("SELECT * FROM gx_user " +
            "WHERE deleted = 0 AND (nickname LIKE CONCAT('%', #{kw}, '%') " +
            "   OR community_nickname LIKE CONCAT('%', #{kw}, '%')) " +
            "ORDER BY id LIMIT #{limit}")
    List<User> searchByKeyword(@Param("kw") String kw, @Param("limit") int limit);

    /**
     * 活跃用户：按发帖数 + 评论数实时统计排行（展示名优先取社区昵称）。
     * community 非空时只统计该社区的数据，避免心声社区出现育儿社区的活跃榜。
     */
    @Select("SELECT u.id AS userId, COALESCE(NULLIF(u.community_nickname, ''), u.nickname) AS nickname, " +
            "COALESCE(p.cnt, 0) AS postCount, COALESCE(c.cnt, 0) AS commentCount, " +
            "(COALESCE(p.cnt, 0) + COALESCE(c.cnt, 0)) AS totalCount " +
            "FROM gx_user u " +
            "LEFT JOIN (SELECT author_id, COUNT(*) AS cnt FROM gx_post " +
            "           WHERE deleted = 0 AND (#{community} IS NULL OR community = #{community}) " +
            "           GROUP BY author_id) p ON p.author_id = u.id " +
            "LEFT JOIN (SELECT cm.user_id, COUNT(*) AS cnt FROM gx_comment cm " +
            "           JOIN gx_post po ON po.id = cm.post_id " +
            "           WHERE cm.deleted = 0 AND po.deleted = 0 " +
            "             AND (#{community} IS NULL OR po.community = #{community}) " +
            "           GROUP BY cm.user_id) c ON c.user_id = u.id " +
            "WHERE u.deleted = 0 AND (COALESCE(p.cnt, 0) + COALESCE(c.cnt, 0)) > 0 " +
            "ORDER BY totalCount DESC, postCount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectActiveParents(@Param("limit") int limit,
                                                  @Param("community") String community);
}

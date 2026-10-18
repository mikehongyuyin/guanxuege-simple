package com.guanxuege.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guanxuege.community.entity.UserCircle;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserCircleMapper extends BaseMapper<UserCircle> {

    /** 物理查询用户与圈子的关系记录（包含已逻辑删除的），返回记录 id */
    @Select("SELECT id FROM gx_user_circle WHERE user_id = #{userId} AND circle_id = #{circleId} LIMIT 1")
    Long findRawId(@Param("userId") Long userId, @Param("circleId") Long circleId);

    /** 恢复已退出的圈子关系（deleted 1 -> 0），返回影响行数 */
    @Update("UPDATE gx_user_circle SET deleted = 0 WHERE user_id = #{userId} AND circle_id = #{circleId} AND deleted = 1")
    int restore(@Param("userId") Long userId, @Param("circleId") Long circleId);
}

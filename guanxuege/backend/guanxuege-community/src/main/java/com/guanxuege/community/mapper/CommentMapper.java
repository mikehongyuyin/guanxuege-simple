package com.guanxuege.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guanxuege.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}

package com.guanxuege.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guanxuege.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

package com.guanxuege.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guanxuege.auth.dto.LoginDTO;
import com.guanxuege.auth.dto.RegisterDTO;
import com.guanxuege.auth.entity.Child;
import com.guanxuege.auth.entity.User;
import com.guanxuege.auth.mapper.ChildMapper;
import com.guanxuege.auth.mapper.UserMapper;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.dto.UserBrief;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ChildMapper childMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public UserServiceImpl(UserMapper userMapper, ChildMapper childMapper) {
        this.userMapper = userMapper;
        this.childMapper = childMapper;
    }

    @Override
    public String login(LoginDTO dto) {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getPhone, dto.getPhone()));
        if (user == null || !encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用");
        }
        return JwtUtil.createToken(user.getId(), user.getPhone());
    }

    @Override
    public User register(RegisterDTO dto) {
        Long count = userMapper.selectCount(
                Wrappers.<User>lambdaQuery().eq(User::getPhone, dto.getPhone()));
        if (count != null && count > 0) {
            throw new BusinessException("该手机号已注册");
        }
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRole(1);
        user.setStatus(1);
        userMapper.insert(user);
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public UserBrief getBrief(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        UserBrief brief = new UserBrief();
        BeanUtils.copyProperties(user, brief);
        return brief;
    }

    @Override
    public List<Child> listChildren(Long parentId) {
        return childMapper.selectList(
                Wrappers.<Child>lambdaQuery().eq(Child::getParentId, parentId));
    }
}

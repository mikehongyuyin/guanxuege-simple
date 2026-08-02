package com.guanxuege.auth.service;

import com.guanxuege.auth.dto.LoginDTO;
import com.guanxuege.auth.dto.RegisterDTO;
import com.guanxuege.auth.entity.Child;
import com.guanxuege.auth.entity.User;
import com.guanxuege.common.dto.UserBrief;

import java.util.List;

public interface UserService {

    /** 登录：校验密码并签发 JWT，返回 token */
    String login(LoginDTO dto);

    /** 注册：默认创建「家长」角色账户 */
    User register(RegisterDTO dto);

    User getById(Long id);

    /** 服务间调用：返回脱敏后的简要信息 */
    UserBrief getBrief(Long userId);

    List<Child> listChildren(Long parentId);
}

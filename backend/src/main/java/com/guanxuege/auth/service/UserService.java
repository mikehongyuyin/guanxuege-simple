package com.guanxuege.auth.service;

import com.guanxuege.auth.dto.LoginDTO;
import com.guanxuege.auth.dto.RegisterDTO;
import com.guanxuege.auth.dto.UserProfileUpdateDTO;
import com.guanxuege.auth.entity.Child;
import com.guanxuege.auth.entity.User;
import com.guanxuege.auth.vo.UserProfileVO;
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

    /**
     * 用户资料：查看他人时只返回公开字段，联系方式（手机/微信/邮箱）一律不返回。
     */
    UserProfileVO getProfile(Long targetId, Long viewerId);

    /** 更新当前登录用户的资料（昵称 / 社区昵称 / 微信 / 邮箱 / 签名） */
    UserProfileVO updateProfile(Long userId, UserProfileUpdateDTO dto);

    /** 按昵称关键字搜索用户，供 @提及选择器使用 */
    List<UserBrief> search(String keyword, int limit);
}

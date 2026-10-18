package com.guanxuege.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guanxuege.auth.ConsentConstants;
import com.guanxuege.auth.dto.LoginDTO;
import com.guanxuege.auth.dto.RegisterDTO;
import com.guanxuege.auth.dto.UserProfileUpdateDTO;
import com.guanxuege.auth.entity.Child;
import com.guanxuege.auth.entity.User;
import com.guanxuege.auth.mapper.ChildMapper;
import com.guanxuege.auth.mapper.UserMapper;
import com.guanxuege.auth.service.ConsentService;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.auth.vo.UserProfileVO;
import com.guanxuege.common.dto.UserBrief;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.util.JwtUtil;
import com.guanxuege.community.entity.Comment;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.mapper.CommentMapper;
import com.guanxuege.community.mapper.PostMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    /** 邮箱格式：只做基本形状校验，不做可达性验证 */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final int MAX_NICKNAME = 50;
    private static final int MAX_COMMUNITY_NICKNAME = 30;
    private static final int MAX_SIGNATURE = 120;
    private static final int MAX_CONTACT = 64;

    private final UserMapper userMapper;
    private final ChildMapper childMapper;
    private final ConsentService consentService;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public UserServiceImpl(UserMapper userMapper, ChildMapper childMapper, ConsentService consentService,
                           PostMapper postMapper, CommentMapper commentMapper) {
        this.userMapper = userMapper;
        this.childMapper = childMapper;
        this.consentService = consentService;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
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
        if (dto.getPrivacyAgreed() == null || !dto.getPrivacyAgreed()) {
            throw new BusinessException("请先阅读并同意《用户协议》与《隐私政策》");
        }
        Long count = userMapper.selectCount(
                Wrappers.<User>lambdaQuery().eq(User::getPhone, dto.getPhone()));
        if (count != null && count > 0) {
            throw new BusinessException("此手机号已存在");
        }
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRole(1);
        user.setStatus(1);
        userMapper.insert(user);
        // 记录《用户协议》《隐私政策》同意（监护人专项同意的一部分，注册阶段）
        consentService.record(user.getId(), null, ConsentConstants.TYPE_USER_REGISTER,
                dto.getConsentVersion() == null ? ConsentConstants.PRIVACY_VERSION : dto.getConsentVersion(),
                null);
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "userBrief", key = "#userId")
    public UserBrief getBrief(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        UserBrief brief = new UserBrief();
        BeanUtils.copyProperties(user, brief);
        brief.setDisplayName(displayNameOf(user));
        return brief;
    }

    @Override
    public List<Child> listChildren(Long parentId) {
        return childMapper.selectList(
                Wrappers.<Child>lambdaQuery().eq(Child::getParentId, parentId));
    }

    @Override
    public UserProfileVO getProfile(Long targetId, Long viewerId) {
        if (targetId == null) {
            throw new BusinessException("用户不存在");
        }
        User user = userMapper.selectById(targetId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        boolean self = viewerId != null && viewerId.equals(targetId);

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setCommunityNickname(user.getCommunityNickname());
        vo.setDisplayName(displayNameOf(user));
        vo.setAvatar(user.getAvatar());
        vo.setSignature(user.getSignature());
        vo.setSelf(self);
        vo.setCreateTime(user.getCreateTime());
        // 联系方式：仅本人可见，避免公开社区里被批量抓取
        if (self) {
            vo.setPhone(user.getPhone());
            vo.setWechat(user.getWechat());
            vo.setEmail(user.getEmail());
        }
        vo.setPostCount(countPosts(user.getId()));
        vo.setCommentCount(countComments(user.getId()));
        return vo;
    }

    @Override
    @CacheEvict(value = "userBrief", key = "#userId")
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateProfile(Long userId, UserProfileUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (dto.getNickname() != null) {
            String nickname = dto.getNickname().trim();
            if (nickname.length() > MAX_NICKNAME) {
                throw new BusinessException("昵称不能超过 " + MAX_NICKNAME + " 个字符");
            }
            user.setNickname(nickname);
        }
        if (dto.getCommunityNickname() != null) {
            String cn = dto.getCommunityNickname().trim();
            if (cn.length() > MAX_COMMUNITY_NICKNAME) {
                throw new BusinessException("社区昵称不能超过 " + MAX_COMMUNITY_NICKNAME + " 个字符");
            }
            // 留空表示清除，社区内回落到账号昵称
            user.setCommunityNickname(cn.isEmpty() ? null : cn);
        }
        if (dto.getWechat() != null) {
            String wechat = dto.getWechat().trim();
            if (wechat.length() > MAX_CONTACT) {
                throw new BusinessException("微信号过长");
            }
            user.setWechat(wechat.isEmpty() ? null : wechat);
        }
        if (dto.getEmail() != null) {
            String email = dto.getEmail().trim();
            if (!email.isEmpty()) {
                if (email.length() > MAX_CONTACT) {
                    throw new BusinessException("邮箱过长");
                }
                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    throw new BusinessException("邮箱格式不正确");
                }
                user.setEmail(email);
            } else {
                user.setEmail(null);
            }
        }
        if (dto.getSignature() != null) {
            String signature = dto.getSignature().trim();
            if (signature.length() > MAX_SIGNATURE) {
                throw new BusinessException("签名不能超过 " + MAX_SIGNATURE + " 个字符");
            }
            user.setSignature(signature.isEmpty() ? null : signature);
        }
        userMapper.updateById(user);
        return getProfile(userId, userId);
    }

    @Override
    public List<UserBrief> search(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        int size = Math.min(Math.max(limit, 1), 20);
        List<User> users = userMapper.searchByKeyword(keyword.trim(), size);
        if (users == null) {
            return List.of();
        }
        return users.stream().map(u -> {
            UserBrief brief = new UserBrief();
            brief.setId(u.getId());
            brief.setNickname(u.getNickname());
            brief.setCommunityNickname(u.getCommunityNickname());
            brief.setDisplayName(displayNameOf(u));
            brief.setAvatar(u.getAvatar());
            return brief;
        }).toList();
    }

    private int countPosts(Long userId) {
        Long n = postMapper.selectCount(
                Wrappers.<Post>lambdaQuery().eq(Post::getAuthorId, userId));
        return n == null ? 0 : n.intValue();
    }

    private int countComments(Long userId) {
        Long n = commentMapper.selectCount(
                Wrappers.<Comment>lambdaQuery().eq(Comment::getUserId, userId));
        return n == null ? 0 : n.intValue();
    }

    /** 社区展示名：社区昵称优先，否则账号昵称 */
    private String displayNameOf(User user) {
        if (user.getCommunityNickname() != null && !user.getCommunityNickname().isBlank()) {
            return user.getCommunityNickname();
        }
        return user.getNickname() == null ? "" : user.getNickname();
    }
}

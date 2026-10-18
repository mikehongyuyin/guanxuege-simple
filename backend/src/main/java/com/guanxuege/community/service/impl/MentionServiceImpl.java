package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guanxuege.auth.entity.User;
import com.guanxuege.auth.mapper.UserMapper;
import com.guanxuege.community.entity.Mention;
import com.guanxuege.community.mapper.MentionMapper;
import com.guanxuege.community.service.MentionService;
import com.guanxuege.community.util.MentionParser;
import com.guanxuege.community.vo.UserMentionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MentionServiceImpl implements MentionService {

    /** 单条内容最多处理的 @ 数量，防止正文里塞满 @ 拖慢发布 */
    private static final int MAX_MENTIONS = 20;

    @Autowired
    private MentionMapper mentionMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UserMentionVO> resolve(String content) {
        List<String> names = MentionParser.parseNames(content);
        if (names.isEmpty()) {
            return List.of();
        }
        Map<Long, UserMentionVO> result = new LinkedHashMap<>();
        for (String name : names) {
            if (result.size() >= MAX_MENTIONS) {
                break;
            }
            User user = matchUser(name);
            if (user == null) {
                continue; // @ 了不存在的昵称：忽略，不产生提及
            }
            if (result.containsKey(user.getId())) {
                continue;
            }
            UserMentionVO vo = new UserMentionVO();
            vo.setUserId(user.getId());
            vo.setNickname(displayNameOf(user));
            result.put(user.getId(), vo);
        }
        return new ArrayList<>(result.values());
    }

    @Override
    public void save(String bizType, Long bizId, String content) {
        remove(bizType, bizId);
        List<UserMentionVO> mentions = resolve(content);
        for (UserMentionVO vo : mentions) {
            Mention mention = new Mention();
            mention.setBizType(bizType);
            mention.setBizId(bizId);
            mention.setUserId(vo.getUserId());
            mentionMapper.insert(mention);
        }
    }

    @Override
    public List<UserMentionVO> list(String bizType, Long bizId) {
        List<Mention> rows = mentionMapper.selectList(
                Wrappers.<Mention>lambdaQuery()
                        .eq(Mention::getBizType, bizType)
                        .eq(Mention::getBizId, bizId));
        if (rows.isEmpty()) {
            return List.of();
        }
        List<UserMentionVO> vos = new ArrayList<>();
        for (Mention row : rows) {
            User user = userMapper.selectById(row.getUserId());
            if (user == null) {
                continue; // 用户已注销：不再展示提及
            }
            UserMentionVO vo = new UserMentionVO();
            vo.setUserId(user.getId());
            vo.setNickname(displayNameOf(user));
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public Map<Long, List<UserMentionVO>> mapByBizIds(String bizType, Collection<Long> bizIds) {
        Map<Long, List<UserMentionVO>> map = new HashMap<>();
        if (bizIds == null || bizIds.isEmpty()) {
            return map;
        }
        List<Mention> rows = mentionMapper.selectList(
                Wrappers.<Mention>lambdaQuery()
                        .eq(Mention::getBizType, bizType)
                        .in(Mention::getBizId, bizIds));
        for (Mention row : rows) {
            map.computeIfAbsent(row.getBizId(), k -> new ArrayList<>());
        }
        // 补齐昵称：一次查出全部被 @ 用户，避免逐条查询
        Map<Long, String> names = new HashMap<>();
        for (Mention row : rows) {
            if (!names.containsKey(row.getUserId())) {
                User user = userMapper.selectById(row.getUserId());
                names.put(row.getUserId(), user == null ? null : displayNameOf(user));
            }
        }
        for (Mention row : rows) {
            String name = names.get(row.getUserId());
            if (name == null) {
                continue; // 用户已注销：不再展示提及
            }
            UserMentionVO vo = new UserMentionVO();
            vo.setUserId(row.getUserId());
            vo.setNickname(name);
            map.get(row.getBizId()).add(vo);
        }
        return map;
    }

    @Override
    public void remove(String bizType, Long bizId) {
        mentionMapper.delete(Wrappers.<Mention>lambdaQuery()
                .eq(Mention::getBizType, bizType)
                .eq(Mention::getBizId, bizId));
    }

    /**
     * 昵称匹配：社区昵称优先于账号昵称（与社区展示名保持一致）。
     */
    private User matchUser(String name) {
        List<User> users = userMapper.selectByDisplayNameOrNickname(name);
        if (users == null || users.isEmpty()) {
            return null;
        }
        for (User u : users) {
            if (u.getCommunityNickname() != null && u.getCommunityNickname().equalsIgnoreCase(name)) {
                return u;
            }
        }
        for (User u : users) {
            if (u.getNickname() != null && u.getNickname().equalsIgnoreCase(name)) {
                return u;
            }
        }
        return users.get(0);
    }

    private String displayNameOf(User user) {
        if (user.getCommunityNickname() != null && !user.getCommunityNickname().isBlank()) {
            return user.getCommunityNickname();
        }
        return user.getNickname() == null ? "" : user.getNickname();
    }

}

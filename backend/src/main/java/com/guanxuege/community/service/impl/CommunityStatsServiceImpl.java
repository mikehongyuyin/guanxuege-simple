package com.guanxuege.community.service.impl;

import com.guanxuege.auth.mapper.UserMapper;
import com.guanxuege.community.service.CommunityStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CommunityStatsServiceImpl implements CommunityStatsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Map<String, Object>> activeParents(int limit, String community) {
        return userMapper.selectActiveParents(limit, community);
    }
}

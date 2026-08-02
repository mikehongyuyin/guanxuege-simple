package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.community.entity.Topic;
import com.guanxuege.community.mapper.TopicMapper;
import com.guanxuege.community.service.TopicService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Override
    public List<Topic> hotList(int limit) {
        return baseMapper.selectList(
                Wrappers.<Topic>lambdaQuery()
                        .orderByDesc(Topic::getRank)
                        .orderByDesc(Topic::getDiscussCount)
                        .last("LIMIT " + limit));
    }
}

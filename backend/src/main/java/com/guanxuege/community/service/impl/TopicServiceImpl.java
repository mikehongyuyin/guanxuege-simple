package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.community.entity.Topic;
import com.guanxuege.community.mapper.PostMapper;
import com.guanxuege.community.mapper.TopicMapper;
import com.guanxuege.community.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Autowired
    private PostMapper postMapper;

    /**
     * 热议话题：实时聚合社区帖子标签（帖子数 + 评论数），不再使用静态预置数据。
     * community 非空时只统计该社区，避免两个社区的话题榜互相串。
     */
    @Override
    public List<Topic> hotList(int limit, String community) {
        List<Map<String, Object>> rows = postMapper.selectHotTags(limit, community);
        List<Topic> list = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> row : rows) {
            Topic t = new Topic();
            t.setId((long) rank);
            t.setRank(rank);
            t.setName(String.valueOf(row.get("tag")));
            long postCount = ((Number) row.get("postCount")).longValue();
            long commentCount = ((Number) row.get("commentCount")).longValue();
            t.setDiscussCount((int) (postCount + commentCount));
            list.add(t);
            rank++;
        }
        return list;
    }
}

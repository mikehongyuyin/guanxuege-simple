package com.guanxuege.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guanxuege.community.entity.Topic;

import java.util.List;

public interface TopicService extends IService<Topic> {

    /** 热议话题榜（按讨论数 / 权重排序） */
    List<Topic> hotList(int limit);
}

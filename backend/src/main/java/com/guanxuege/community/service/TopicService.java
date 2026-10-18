package com.guanxuege.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guanxuege.community.entity.Topic;

import java.util.List;

public interface TopicService extends IService<Topic> {

    /** 热议话题榜（按讨论数 / 权重排序）。community 为空时统计全部社区 */
    List<Topic> hotList(int limit, String community);

    /** 兼容旧调用：统计全部社区 */
    default List<Topic> hotList(int limit) {
        return hotList(limit, null);
    }
}

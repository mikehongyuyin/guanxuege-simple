package com.guanxuege.community.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guanxuege.common.constant.MqConstant;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.entity.Topic;
import com.guanxuege.community.mapper.PostMapper;
import com.guanxuege.community.mapper.TopicMapper;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 发帖事件消费者：
 * 1) 帖子热度加成
 * 2) 同步更新同名话题的讨论数
 */
@Component
@RocketMQMessageListener(
        topic = MqConstant.TOPIC_COMMUNITY,
        selectorExpression = MqConstant.TAG_POST_CREATED,
        consumerGroup = MqConstant.GROUP_POST_CONSUMER,
        messageModel = MessageModel.CLUSTERING)
public class PostEventConsumer implements RocketMQListener<String> {

    private final PostMapper postMapper;
    private final TopicMapper topicMapper;

    public PostEventConsumer(PostMapper postMapper, TopicMapper topicMapper) {
        this.postMapper = postMapper;
        this.topicMapper = topicMapper;
    }

    @Override
    public void onMessage(String postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return;
        }
        // 热度加成
        post.setHotScore((post.getHotScore() == null ? 0 : post.getHotScore()) + 10);
        postMapper.updateById(post);

        // 话题讨论数 +1（按标签匹配）
        if (post.getTag() != null) {
            Topic topic = topicMapper.selectOne(
                    Wrappers.<Topic>lambdaQuery().eq(Topic::getName, post.getTag()));
            if (topic != null) {
                topic.setDiscussCount((topic.getDiscussCount() == null ? 0 : topic.getDiscussCount()) + 1);
                topicMapper.updateById(topic);
            }
        }
    }
}

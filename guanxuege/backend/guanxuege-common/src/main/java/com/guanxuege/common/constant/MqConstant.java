package com.guanxuege.common.constant;

/**
 * RocketMQ Topic / Tag / 消费组 常量
 */
public class MqConstant {
    /** 社区事件主题 */
    public static final String TOPIC_COMMUNITY = "gxg-community-topic";
    /** 发帖事件 Tag */
    public static final String TAG_POST_CREATED = "POST_CREATED";
    /** 成绩异常告警 Tag */
    public static final String TAG_GRADE_ALERT = "GRADE_ALERT";
    /** 发帖事件消费组 */
    public static final String GROUP_POST_CONSUMER = "gxg-post-consumer";
}

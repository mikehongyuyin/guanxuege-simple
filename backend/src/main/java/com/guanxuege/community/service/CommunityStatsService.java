package com.guanxuege.community.service;

import java.util.List;
import java.util.Map;

/**
 * 社区统计（真实数据）
 */
public interface CommunityStatsService {

    /** 活跃用户排行（按发帖数 + 评论数）。community 为空时统计全部社区 */
    List<Map<String, Object>> activeParents(int limit, String community);

    /** 兼容旧调用：统计全部社区 */
    default List<Map<String, Object>> activeParents(int limit) {
        return activeParents(limit, null);
    }
}

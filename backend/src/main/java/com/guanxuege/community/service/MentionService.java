package com.guanxuege.community.service;

import com.guanxuege.community.vo.UserMentionVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @提及：把正文里的「@昵称」解析成真实用户并落库。
 * <p>
 * 解析与存储分离的意义在于：@ 了一个不存在的名字不会报错，只是不产生提及关系；
 * 用户后来改了昵称，旧的提及关系仍保留（记录的是用户 id，不是字符串）。
 */
public interface MentionService {

    /** 解析正文，返回真实存在的被 @ 用户（按出现顺序去重） */
    List<UserMentionVO> resolve(String content);

    /** 保存某条内容的提及关系：先清后插，保证与最新正文一致 */
    void save(String bizType, Long bizId, String content);

    /** 查询某条内容的提及关系 */
    List<UserMentionVO> list(String bizType, Long bizId);

    /** 批量查询，避免逐条查询导致 N+1 */
    Map<Long, List<UserMentionVO>> mapByBizIds(String bizType, Collection<Long> bizIds);

    /** 删除某条内容的全部提及关系 */
    void remove(String bizType, Long bizId);
}

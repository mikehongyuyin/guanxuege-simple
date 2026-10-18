package com.guanxuege.auth.service;

import com.guanxuege.auth.entity.ConsentRecord;

import java.util.List;

public interface ConsentService {

    /**
     * 记录一次同意（agreed=1，agreedAt=now）
     */
    ConsentRecord record(Long parentId, Long childId, String consentType, String version, String guardianRelation);

    /**
     * 撤回：将该家长下匹配的、仍处于同意状态的记录置为 agreed=0，并写入撤回时间。
     * 保留历史记录以满足审计与可撤回要求。
     */
    void withdraw(Long parentId, Long childId, String consentType);

    /**
     * 查询该家长对指定类型最新一条「有效（已同意）」的记录。
     */
    ConsentRecord getLatestValid(Long parentId, Long childId, String consentType);

    /**
     * 列出该家长全部同意记录（按时间倒序），用于设置页展示与撤回。
     */
    List<ConsentRecord> listByParent(Long parentId);
}

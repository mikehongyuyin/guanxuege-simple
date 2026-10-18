package com.guanxuege.community.dto;

import lombok.Data;

import java.util.List;

/**
 * 帖子一键转计划请求体
 */
@Data
public class PostToPlanDTO {

    /** 孩子 id */
    private Long childId;

    /** 计划标题（预填帖子标题） */
    private String title;

    /** 初始任务标题列表 */
    private List<String> taskTitles;
}

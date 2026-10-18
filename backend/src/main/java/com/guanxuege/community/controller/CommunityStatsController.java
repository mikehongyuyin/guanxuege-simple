package com.guanxuege.community.controller;

import com.guanxuege.common.R;
import com.guanxuege.community.service.CommunityStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityStatsController {

    @Autowired
    private CommunityStatsService communityStatsService;

    /** 活跃用户：按发帖 + 评论数统计的真实排行。传 community 只统计该社区 */
    @GetMapping("/active-parents")
    public R<List<Map<String, Object>>> activeParents(@RequestParam(defaultValue = "5") int limit,
                                                     @RequestParam(required = false) String community) {
        return R.ok(communityStatsService.activeParents(limit, community));
    }
}

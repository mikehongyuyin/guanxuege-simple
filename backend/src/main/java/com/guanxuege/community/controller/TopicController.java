package com.guanxuege.community.controller;

import com.guanxuege.common.R;
import com.guanxuege.community.entity.Topic;
import com.guanxuege.community.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topic")
public class TopicController {

    @Autowired
    private TopicService topicService;

    /** 热议话题：传 community 只统计该社区，不传则统计全部 */
    @GetMapping("/hot")
    public R<List<Topic>> hot(@RequestParam(defaultValue = "10") int limit,
                              @RequestParam(required = false) String community) {
        return R.ok(topicService.hotList(limit, community));
    }
}

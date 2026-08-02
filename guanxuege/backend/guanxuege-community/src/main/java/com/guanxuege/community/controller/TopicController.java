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

    @GetMapping("/hot")
    public R<List<Topic>> hot(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(topicService.hotList(limit));
    }
}

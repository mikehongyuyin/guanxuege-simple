package com.guanxuege.auth.controller;

import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.R;
import com.guanxuege.common.dto.UserBrief;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务间内部接口（仅经网关内部路由调用，不对外暴露）
 * 对应 common 中定义的 OpenFeign 客户端 AuthClient
 */
@RestController
@RequestMapping("/api/auth")
public class UserInternalController {

    @Autowired
    private UserService userService;

    @GetMapping("/internal/{userId}")
    public R<UserBrief> getUserBrief(@PathVariable("userId") Long userId) {
        return R.ok(userService.getBrief(userId));
    }
}

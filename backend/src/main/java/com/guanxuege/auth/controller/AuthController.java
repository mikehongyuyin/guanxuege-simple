package com.guanxuege.auth.controller;

import com.guanxuege.auth.dto.LoginDTO;
import com.guanxuege.auth.dto.RegisterDTO;
import com.guanxuege.auth.entity.User;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        User user = userService.getById(
                com.guanxuege.common.util.JwtUtil.getUserId(token));
        Map<String, Object> data = new HashMap<>(4);
        data.put("token", token);
        data.put("user", user);
        return R.ok(data);
    }

    @PostMapping("/register")
    public R<User> register(@RequestBody RegisterDTO dto) {
        return R.ok(userService.register(dto));
    }

    @GetMapping("/me")
    public R<User> me(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(userService.getById(userId));
    }
}

package com.guanxuege.auth.controller;

import com.guanxuege.auth.dto.UserProfileUpdateDTO;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.auth.vo.UserProfileVO;
import com.guanxuege.common.R;
import com.guanxuege.common.dto.UserBrief;
import com.guanxuege.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户资料：查看与自助维护。
 * <p>
 * 隐私约定：查看他人资料时，手机号 / 微信号 / 邮箱不返回（见 {@link UserProfileVO}）。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** 当前登录用户的完整资料（含联系方式） */
    @GetMapping("/profile")
    public R<UserProfileVO> profile(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(userService.getProfile(userId, userId));
    }

    /** 更新当前登录用户资料 */
    @PutMapping("/profile")
    public R<UserProfileVO> update(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                   @RequestBody UserProfileUpdateDTO dto) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(userService.updateProfile(userId, dto));
    }

    /**
     * 查看指定用户资料：本人返回完整信息，他人只返回公开信息。
     */
    @GetMapping("/{id}")
    public R<UserProfileVO> detail(@PathVariable("id") Long id,
                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return R.ok(userService.getProfile(id, userId));
    }

    /** 昵称关键字搜索：供 @提及选择器使用，需登录 */
    @GetMapping("/search")
    public R<List<UserBrief>> search(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                     @RequestParam(defaultValue = "") String keyword,
                                     @RequestParam(defaultValue = "10") int limit) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(userService.search(keyword, limit));
    }
}

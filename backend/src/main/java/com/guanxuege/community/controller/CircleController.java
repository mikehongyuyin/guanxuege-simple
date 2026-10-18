package com.guanxuege.community.controller;

import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.community.service.CircleService;
import com.guanxuege.community.vo.CircleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分层圈子：手动加入 / 退出
 */
@RestController
@RequestMapping("/api/circle")
public class CircleController {

    @Autowired
    private CircleService circleService;

    /**
     * 圈子列表。community 为空时返回全部；传 parenting / voice 时只返回该社区的圈子。
     */
    @GetMapping("/list")
    public R<List<CircleVO>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                  @RequestParam(required = false) String community) {
        return R.ok(circleService.listAll(userId, community));
    }

    @PostMapping("/{id}/join")
    public R<Boolean> join(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                           @PathVariable("id") Long id) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        circleService.join(userId, id);
        return R.ok(true);
    }

    @DeleteMapping("/{id}/join")
    public R<Boolean> leave(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                            @PathVariable("id") Long id) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        circleService.leave(userId, id);
        return R.ok(true);
    }
}

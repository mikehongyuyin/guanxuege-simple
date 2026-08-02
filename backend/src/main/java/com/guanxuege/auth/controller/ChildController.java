package com.guanxuege.auth.controller;

import com.guanxuege.auth.entity.Child;
import com.guanxuege.auth.service.ChildService;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/child")
public class ChildController {

    @Autowired
    private ChildService childService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public R<List<Child>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(userService.listChildren(userId));
    }

    @PostMapping
    public R<Child> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                           @RequestBody Child child) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        child.setParentId(userId);
        childService.save(child);
        return R.ok(child);
    }

    @PutMapping
    public R<Child> update(@RequestBody Child child) {
        childService.updateById(child);
        return R.ok(child);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.ok(childService.removeById(id));
    }
}

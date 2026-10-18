package com.guanxuege.auth.controller;

import com.guanxuege.auth.dto.ChildCreateDTO;
import com.guanxuege.auth.entity.Child;
import com.guanxuege.auth.service.ChildService;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.exception.ForbiddenException;
import com.guanxuege.common.service.OwnershipService;
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

    @Autowired
    private OwnershipService ownershipService;

    @GetMapping("/list")
    public R<List<Child>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(userService.listChildren(userId));
    }

    /**
     * 绑定孩子：必须携带监护人关系声明 + 儿童信息专项同意，
     * 否则服务端拒绝（见 ChildService.createWithConsent）。
     */
    @PostMapping
    public R<Child> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                           @RequestBody ChildCreateDTO dto) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(childService.createWithConsent(userId, dto));
    }

    @PutMapping
    public R<Child> update(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                           @RequestBody Child child) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (child.getId() == null) {
            throw new BusinessException("请选择要修改的孩子");
        }
        // 越权校验：孩子必须属于当前家长，否则抛 403
        ownershipService.requireChild(userId, child.getId());
        // 监护人不可被篡改，防止把孩子过户到其他账号
        child.setParentId(userId);
        childService.updateById(child);
        return R.ok(child);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                             @PathVariable("id") Long id) {
        // 越权校验：任意登录用户不得删除他人孩子档案（含其同意记录）
        ownershipService.requireChild(userId, id);
        return R.ok(childService.deleteWithConsent(userId, id));
    }
}

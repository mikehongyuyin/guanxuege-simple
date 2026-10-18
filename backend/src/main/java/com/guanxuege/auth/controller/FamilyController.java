package com.guanxuege.auth.controller;

import com.guanxuege.auth.entity.Family;
import com.guanxuege.auth.service.FamilyService;
import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 家庭档案：一个家长一份，创建后通过 PUT 更新
 */
@RestController
@RequestMapping("/api/family")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    @GetMapping
    public R<Family> get(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return R.ok(familyService.getByParent(userId));
    }

    @PostMapping
    public R<Family> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                            @RequestBody Family family) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (familyService.getByParent(userId) != null) {
            throw new BusinessException("家庭档案已存在");
        }
        family.setParentId(userId);
        familyService.save(family);
        return R.ok(family);
    }

    @PutMapping
    public R<Family> update(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                            @RequestBody Family family) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Family exist = familyService.getByParent(userId);
        if (exist == null) {
            family.setParentId(userId);
            familyService.save(family);
            return R.ok(family);
        }
        exist.setFamilyName(family.getFamilyName());
        exist.setFamilyType(family.getFamilyType());
        exist.setMotto(family.getMotto());
        familyService.updateById(exist);
        return R.ok(exist);
    }
}

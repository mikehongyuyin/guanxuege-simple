package com.guanxuege.grade.controller;

import com.guanxuege.common.R;
import com.guanxuege.common.service.OwnershipService;
import com.guanxuege.grade.entity.Grade;
import com.guanxuege.grade.service.GradeService;
import com.guanxuege.grade.vo.GradeOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grade")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private OwnershipService ownershipService;

    @GetMapping("/list")
    public R<List<Grade>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                               @RequestParam Long childId) {
        ownershipService.requireChild(userId, childId);
        return R.ok(gradeService.list(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<Grade>lambdaQuery()
                        .eq(Grade::getChildId, childId)
                        .orderByDesc(Grade::getExamDate)));
    }

    @GetMapping("/overview")
    public R<GradeOverview> overview(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                     @RequestParam Long childId) {
        ownershipService.requireChild(userId, childId);
        return R.ok(gradeService.overview(childId));
    }

    @PostMapping
    public R<Grade> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                           @RequestBody Grade grade) {
        ownershipService.requireChild(userId, grade.getChildId());
        gradeService.save(grade);
        return R.ok(grade);
    }

    @PutMapping
    public R<Grade> update(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                           @RequestBody Grade grade) {
        Grade exist = ownershipService.requireGrade(userId, grade.getId());
        // 允许变更归属孩子，但目标孩子必须同样属于当前家长
        if (grade.getChildId() != null && !grade.getChildId().equals(exist.getChildId())) {
            ownershipService.requireChild(userId, grade.getChildId());
        } else {
            grade.setChildId(exist.getChildId());
        }
        gradeService.updateById(grade);
        return R.ok(grade);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                             @PathVariable("id") Long id) {
        ownershipService.requireGrade(userId, id);
        return R.ok(gradeService.removeById(id));
    }
}

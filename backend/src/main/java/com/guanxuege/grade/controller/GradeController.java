package com.guanxuege.grade.controller;

import com.guanxuege.common.R;
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

    @GetMapping("/list")
    public R<List<Grade>> list(@RequestParam Long childId) {
        return R.ok(gradeService.list(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<Grade>lambdaQuery()
                        .eq(Grade::getChildId, childId)
                        .orderByDesc(Grade::getExamDate)));
    }

    @GetMapping("/overview")
    public R<GradeOverview> overview(@RequestParam Long childId) {
        return R.ok(gradeService.overview(childId));
    }

    @PostMapping
    public R<Grade> create(@RequestBody Grade grade) {
        gradeService.save(grade);
        return R.ok(grade);
    }

    @PutMapping
    public R<Grade> update(@RequestBody Grade grade) {
        gradeService.updateById(grade);
        return R.ok(grade);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.ok(gradeService.removeById(id));
    }
}

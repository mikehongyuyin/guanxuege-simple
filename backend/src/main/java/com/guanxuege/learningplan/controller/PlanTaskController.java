package com.guanxuege.learningplan.controller;

import com.guanxuege.common.R;
import com.guanxuege.learningplan.entity.PlanTask;
import com.guanxuege.learningplan.service.PlanTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plan/task")
public class PlanTaskController {

    @Autowired
    private PlanTaskService planTaskService;

    @GetMapping("/list")
    public R<List<PlanTask>> list(@RequestParam Long planId) {
        return R.ok(planTaskService.list(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<PlanTask>lambdaQuery()
                        .eq(PlanTask::getPlanId, planId)
                        .orderByAsc(PlanTask::getDueDate)));
    }

    @PostMapping
    public R<PlanTask> create(@RequestBody PlanTask task) {
        planTaskService.save(task);
        return R.ok(task);
    }

    @PutMapping
    public R<PlanTask> update(@RequestBody PlanTask task) {
        planTaskService.updateById(task);
        return R.ok(task);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.ok(planTaskService.removeById(id));
    }
}

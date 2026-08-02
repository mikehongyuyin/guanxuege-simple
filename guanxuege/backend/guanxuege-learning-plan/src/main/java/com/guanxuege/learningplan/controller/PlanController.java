package com.guanxuege.learningplan.controller;

import com.guanxuege.common.R;
import com.guanxuege.learningplan.entity.Plan;
import com.guanxuege.learningplan.service.PlanService;
import com.guanxuege.learningplan.vo.PlanOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plan")
public class PlanController {

    @Autowired
    private PlanService planService;

    @GetMapping("/list")
    public R<List<Plan>> list(@RequestParam Long childId) {
        return R.ok(planService.list(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<Plan>lambdaQuery()
                        .eq(Plan::getChildId, childId)
                        .orderByDesc(Plan::getCreateTime)));
    }

    @GetMapping("/overview")
    public R<PlanOverview> overview(@RequestParam Long childId) {
        return R.ok(planService.overview(childId));
    }

    @PostMapping
    public R<Plan> create(@RequestBody Plan plan) {
        planService.save(plan);
        return R.ok(plan);
    }

    @PutMapping
    public R<Plan> update(@RequestBody Plan plan) {
        planService.updateById(plan);
        return R.ok(plan);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.ok(planService.removeById(id));
    }
}

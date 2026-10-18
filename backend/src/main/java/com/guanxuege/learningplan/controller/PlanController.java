package com.guanxuege.learningplan.controller;

import com.guanxuege.common.R;
import com.guanxuege.common.service.OwnershipService;
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

    @Autowired
    private OwnershipService ownershipService;

    @GetMapping("/list")
    public R<List<Plan>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                              @RequestParam Long childId) {
        ownershipService.requireChild(userId, childId);
        return R.ok(planService.list(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<Plan>lambdaQuery()
                        .eq(Plan::getChildId, childId)
                        .orderByDesc(Plan::getCreateTime)));
    }

    @GetMapping("/overview")
    public R<PlanOverview> overview(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                    @RequestParam Long childId) {
        ownershipService.requireChild(userId, childId);
        return R.ok(planService.overview(childId));
    }

    @PostMapping
    public R<Plan> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                          @RequestBody Plan plan) {
        ownershipService.requireChild(userId, plan.getChildId());
        planService.save(plan);
        return R.ok(plan);
    }

    @PutMapping
    public R<Plan> update(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                          @RequestBody Plan plan) {
        Plan exist = ownershipService.requirePlan(userId, plan.getId());
        // 允许变更归属孩子，但目标孩子必须同样属于当前家长
        if (plan.getChildId() != null && !plan.getChildId().equals(exist.getChildId())) {
            ownershipService.requireChild(userId, plan.getChildId());
        } else {
            plan.setChildId(exist.getChildId());
        }
        planService.updateById(plan);
        return R.ok(plan);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                             @PathVariable("id") Long id) {
        ownershipService.requirePlan(userId, id);
        return R.ok(planService.removeById(id));
    }
}

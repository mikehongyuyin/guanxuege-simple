package com.guanxuege.learningplan.controller;

import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.service.OwnershipService;
import com.guanxuege.learningplan.entity.Checkin;
import com.guanxuege.learningplan.entity.Plan;
import com.guanxuege.learningplan.entity.PlanTask;
import com.guanxuege.learningplan.service.CheckinService;
import com.guanxuege.learningplan.service.PlanTaskService;
import com.guanxuege.learningplan.vo.CheckinStatusVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plan/task")
public class PlanTaskController {

    @Autowired
    private PlanTaskService planTaskService;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private OwnershipService ownershipService;

    @GetMapping("/list")
    public R<List<PlanTask>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                  @RequestParam Long planId) {
        ownershipService.requirePlan(userId, planId);
        return R.ok(planTaskService.list(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<PlanTask>lambdaQuery()
                        .eq(PlanTask::getPlanId, planId)
                        .orderByAsc(PlanTask::getDueDate)));
    }

    @PostMapping
    public R<PlanTask> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                              @RequestBody PlanTask task) {
        ownershipService.requirePlan(userId, task.getPlanId());
        planTaskService.save(task);
        return R.ok(task);
    }

    @PutMapping
    public R<PlanTask> update(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                              @RequestBody PlanTask task) {
        PlanTask exist = ownershipService.requireTask(userId, task.getId());
        task.setPlanId(exist.getPlanId());
        planTaskService.updateById(task);
        return R.ok(task);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                             @PathVariable("id") Long id) {
        ownershipService.requireTask(userId, id);
        return R.ok(planTaskService.removeById(id));
    }

    /** 任务打卡：今天对某个任务打卡一次 */
    @PostMapping("/{taskId}/checkin")
    public R<Checkin> checkin(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                              @PathVariable("taskId") Long taskId,
                              @RequestParam(required = false) Long childId,
                              @RequestBody(required = false) Checkin body) {
        PlanTask task = ownershipService.requireTask(userId, taskId);
        if (childId == null && body != null) {
            childId = body.getChildId();
        }
        if (childId == null) {
            throw new BusinessException("请选择孩子");
        }
        // 打卡对象必须是该任务所属计划的孩子，防止跨孩子打卡
        Plan plan = ownershipService.requirePlan(userId, task.getPlanId());
        if (!childId.equals(plan.getChildId())) {
            throw new BusinessException("该任务不属于所选孩子");
        }
        String note = body == null ? "" : body.getNote();
        return R.ok(checkinService.checkin(taskId, childId, note));
    }

    /** 任务打卡状态：今日是否已打卡 / 连续打卡天数 / 累计次数 */
    @GetMapping("/{taskId}/checkin/status")
    public R<CheckinStatusVO> checkinStatus(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                            @PathVariable("taskId") Long taskId,
                                            @RequestParam Long childId) {
        ownershipService.requireTask(userId, taskId);
        ownershipService.requireChild(userId, childId);
        return R.ok(checkinService.status(taskId, childId));
    }
}

package com.guanxuege.common.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.guanxuege.auth.entity.Child;
import com.guanxuege.auth.mapper.ChildMapper;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.exception.ForbiddenException;
import com.guanxuege.grade.entity.Grade;
import com.guanxuege.grade.mapper.GradeMapper;
import com.guanxuege.learningplan.entity.Plan;
import com.guanxuege.learningplan.entity.PlanTask;
import com.guanxuege.learningplan.mapper.PlanMapper;
import com.guanxuege.learningplan.mapper.PlanTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 数据归属校验服务（横切安全能力）
 * <p>
 * 鉴权过滤器只能证明「请求者是谁」，无法证明「资源是请求者的」。
 * 本服务统一补齐后者：孩子核是该产品的数据边界，
 * 计划 / 任务 / 成绩都沿「task -> plan -> child -> parent」链路回溯到家长。
 * <p>
 * 用法：在 Controller 中先调用 requireXxx，再做业务操作。
 * 校验失败抛 {@link ForbiddenException}，由全局异常处理转成 HTTP 403。
 */
@Service
public class OwnershipService {

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private PlanTaskMapper planTaskMapper;

    @Autowired
    private GradeMapper gradeMapper;

    /** 校验孩子属于当前家长 */
    public void requireChild(Long userId, Long childId) {
        requireLogin(userId);
        if (childId == null) {
            throw new BusinessException("请选择孩子");
        }
        Long count = childMapper.selectCount(
                Wrappers.<Child>lambdaQuery()
                        .eq(Child::getId, childId)
                        .eq(Child::getParentId, userId));
        if (count == null || count == 0) {
            throw new ForbiddenException("无权访问该孩子的数据");
        }
    }

    /** 校验并返回计划（同时校验其所属孩子归属） */
    public Plan requirePlan(Long userId, Long planId) {
        requireLogin(userId);
        if (planId == null) {
            throw new BusinessException("请选择计划");
        }
        Plan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new ForbiddenException("计划不存在");
        }
        requireChild(userId, plan.getChildId());
        return plan;
    }

    /** 校验并返回任务（沿 任务 -> 计划 -> 孩子 链路回溯归属） */
    public PlanTask requireTask(Long userId, Long taskId) {
        requireLogin(userId);
        if (taskId == null) {
            throw new BusinessException("请选择任务");
        }
        PlanTask task = planTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ForbiddenException("任务不存在");
        }
        requirePlan(userId, task.getPlanId());
        return task;
    }

    /** 校验并返回成绩（同时校验其所属孩子归属） */
    public Grade requireGrade(Long userId, Long gradeId) {
        requireLogin(userId);
        if (gradeId == null) {
            throw new BusinessException("请选择成绩记录");
        }
        Grade grade = gradeMapper.selectById(gradeId);
        if (grade == null) {
            throw new ForbiddenException("成绩记录不存在");
        }
        requireChild(userId, grade.getChildId());
        return grade;
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
    }
}

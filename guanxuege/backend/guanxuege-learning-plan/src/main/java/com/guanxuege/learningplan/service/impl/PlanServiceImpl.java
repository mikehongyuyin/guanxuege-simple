package com.guanxuege.learningplan.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.learningplan.entity.Plan;
import com.guanxuege.learningplan.entity.PlanTask;
import com.guanxuege.learningplan.mapper.PlanMapper;
import com.guanxuege.learningplan.mapper.PlanTaskMapper;
import com.guanxuege.learningplan.service.PlanService;
import com.guanxuege.learningplan.vo.PlanOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements PlanService {

    @Autowired
    private PlanTaskMapper planTaskMapper;

    @Override
    public PlanOverview overview(Long childId) {
        PlanOverview vo = new PlanOverview();
        vo.setTodoCount(countStatus(childId, 0));
        vo.setDoingCount(countStatus(childId, 1));
        vo.setDoneCount(countStatus(childId, 2));

        Long taskTotal = planTaskMapper.selectCount(
                Wrappers.<PlanTask>lambdaQuery().in(PlanTask::getPlanId,
                        baseMapper.selectList(Wrappers.<Plan>lambdaQuery()
                                        .eq(Plan::getChildId, childId))
                                .stream().map(Plan::getId).toList()));
        Long taskDone = planTaskMapper.selectCount(
                Wrappers.<PlanTask>lambdaQuery().eq(PlanTask::getStatus, 2)
                        .in(PlanTask::getPlanId,
                                baseMapper.selectList(Wrappers.<Plan>lambdaQuery()
                                                .eq(Plan::getChildId, childId))
                                        .stream().map(Plan::getId).toList()));
        vo.setTaskTotal(taskTotal == null ? 0L : taskTotal);
        vo.setTaskDone(taskDone == null ? 0L : taskDone);
        vo.setOverallProgress(taskTotal != null && taskTotal > 0
                ? (int) (taskDone * 100 / taskTotal) : 0);
        return vo;
    }

    private Long countStatus(Long childId, Integer status) {
        Long c = baseMapper.selectCount(Wrappers.<Plan>lambdaQuery()
                .eq(Plan::getChildId, childId)
                .eq(Plan::getStatus, status));
        return c == null ? 0L : c;
    }
}

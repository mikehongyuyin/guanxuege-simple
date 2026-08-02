package com.guanxuege.learningplan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guanxuege.learningplan.entity.Plan;
import com.guanxuege.learningplan.vo.PlanOverview;

public interface PlanService extends IService<Plan> {

    /** 概览：按状态统计计划与任务、整体进度 */
    PlanOverview overview(Long childId);
}

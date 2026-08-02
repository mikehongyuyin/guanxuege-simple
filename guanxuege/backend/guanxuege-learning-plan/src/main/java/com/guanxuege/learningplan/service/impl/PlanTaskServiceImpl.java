package com.guanxuege.learningplan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.learningplan.entity.PlanTask;
import com.guanxuege.learningplan.mapper.PlanTaskMapper;
import com.guanxuege.learningplan.service.PlanTaskService;
import org.springframework.stereotype.Service;

@Service
public class PlanTaskServiceImpl extends ServiceImpl<PlanTaskMapper, PlanTask> implements PlanTaskService {
}

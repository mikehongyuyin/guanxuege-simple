package com.guanxuege.grade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guanxuege.grade.entity.Grade;
import com.guanxuege.grade.vo.GradeOverview;

public interface GradeService extends IService<Grade> {

    /** 概览：总分平均 + 各科统计 */
    GradeOverview overview(Long childId);
}

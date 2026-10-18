package com.guanxuege.learningplan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guanxuege.learningplan.entity.Checkin;
import com.guanxuege.learningplan.vo.CheckinStatusVO;

public interface CheckinService extends IService<Checkin> {

    /** 今日打卡（同日重复打卡会抛出业务异常） */
    Checkin checkin(Long taskId, Long childId, String note);

    /** 查询打卡状态：今日是否已打卡、连续打卡天数、累计次数 */
    CheckinStatusVO status(Long taskId, Long childId);
}

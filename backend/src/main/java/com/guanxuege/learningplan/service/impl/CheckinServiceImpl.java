package com.guanxuege.learningplan.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.learningplan.entity.Checkin;
import com.guanxuege.learningplan.mapper.CheckinMapper;
import com.guanxuege.learningplan.service.CheckinService;
import com.guanxuege.learningplan.vo.CheckinStatusVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CheckinServiceImpl extends ServiceImpl<CheckinMapper, Checkin> implements CheckinService {

    @Override
    public Checkin checkin(Long taskId, Long childId, String note) {
        LocalDate today = LocalDate.now();
        Long count = baseMapper.selectCount(
                Wrappers.<Checkin>lambdaQuery()
                        .eq(Checkin::getTaskId, taskId)
                        .eq(Checkin::getCheckinDate, today));
        if (count != null && count > 0) {
            throw new BusinessException("今天已经打卡啦，明天继续加油");
        }
        Checkin checkin = new Checkin();
        checkin.setTaskId(taskId);
        checkin.setChildId(childId);
        checkin.setCheckinDate(today);
        checkin.setNote(note == null ? "" : note);
        baseMapper.insert(checkin);
        return checkin;
    }

    @Override
    public CheckinStatusVO status(Long taskId, Long childId) {
        List<Checkin> records = baseMapper.selectList(
                Wrappers.<Checkin>lambdaQuery()
                        .eq(Checkin::getTaskId, taskId)
                        .eq(Checkin::getChildId, childId));
        Set<LocalDate> dates = new HashSet<>();
        for (Checkin c : records) {
            dates.add(c.getCheckinDate());
        }
        CheckinStatusVO vo = new CheckinStatusVO();
        LocalDate today = LocalDate.now();
        vo.setCheckedToday(dates.contains(today));

        // 连续打卡：今天已打卡从今天起算，否则从昨天起算
        LocalDate cursor = dates.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        vo.setStreak(streak);
        vo.setTotalCount(dates.size());
        return vo;
    }
}

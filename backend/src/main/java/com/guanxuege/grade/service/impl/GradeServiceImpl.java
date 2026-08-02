package com.guanxuege.grade.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanxuege.grade.entity.Grade;
import com.guanxuege.grade.mapper.GradeMapper;
import com.guanxuege.grade.service.GradeService;
import com.guanxuege.grade.vo.GradeOverview;
import com.guanxuege.grade.vo.SubjectStat;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements GradeService {

    @Override
    public GradeOverview overview(Long childId) {
        List<Grade> grades = baseMapper.selectList(
                Wrappers.<Grade>lambdaQuery().eq(Grade::getChildId, childId));

        Map<String, BigDecimal> sumMap = new LinkedHashMap<>();
        Map<String, BigDecimal> fullMap = new LinkedHashMap<>();
        Map<String, Long> countMap = new LinkedHashMap<>();

        for (Grade g : grades) {
            sumMap.merge(g.getSubject(), g.getScore(), BigDecimal::add);
            fullMap.putIfAbsent(g.getSubject(), g.getFullScore());
            countMap.merge(g.getSubject(), 1L, Long::sum);
        }

        GradeOverview overview = new GradeOverview();
        BigDecimal overallSum = BigDecimal.ZERO;
        int subjectCount = 0;
        List<SubjectStat> stats = new java.util.ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : sumMap.entrySet()) {
            String subject = e.getKey();
            BigDecimal avg = e.getValue().divide(BigDecimal.valueOf(countMap.get(subject)), 2, RoundingMode.HALF_UP);
            SubjectStat stat = new SubjectStat();
            stat.setSubject(subject);
            stat.setAverage(avg);
            stat.setFullScore(fullMap.get(subject));
            BigDecimal full = fullMap.get(subject);
            stat.setRate(full != null && full.signum() > 0
                    ? avg.multiply(BigDecimal.valueOf(100)).divide(full, 0, RoundingMode.HALF_UP).intValue() : 0);
            stat.setCount(countMap.get(subject));
            stats.add(stat);
            overallSum = overallSum.add(avg);
            subjectCount++;
        }
        overview.setSubjectStats(stats);
        overview.setOverallAverage(subjectCount > 0
                ? overallSum.divide(BigDecimal.valueOf(subjectCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return overview;
    }
}

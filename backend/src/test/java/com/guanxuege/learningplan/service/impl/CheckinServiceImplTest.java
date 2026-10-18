package com.guanxuege.learningplan.service.impl;

import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.learningplan.entity.Checkin;
import com.guanxuege.learningplan.mapper.CheckinMapper;
import com.guanxuege.learningplan.vo.CheckinStatusVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 打卡核心不变量测试。
 * <p>
 * 最关键的一条业务规则：连续天数的计算允许「今天还没打卡」。
 * 只要昨天及之前是连续的，连续天数就不能被误判为中断——
 * 否则用户每天早上打开 App 都会看到自己的连续记录被清零。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class CheckinServiceImplTest {

    private static final Long TASK_ID = 55L;
    private static final Long CHILD_ID = 66L;

    private final LocalDate today = LocalDate.now();

    @Mock
    private CheckinMapper checkinMapper;

    @InjectMocks
    private CheckinServiceImpl checkinService;

    @BeforeEach
    void bindBaseMapper() {
        // baseMapper 来自 MyBatis-Plus 的 ServiceImpl 父类，显式绑定以避免泛型擦除导致的注入歧义。
        ReflectionTestUtils.setField(checkinService, "baseMapper", checkinMapper);
    }

    // ------------------------------------------------------------------
    // 连续天数
    // ------------------------------------------------------------------

    @Test
    @DisplayName("今天已打卡且之前连续：连续天数从今天起算，逐日累加")
    void status_continuousIncludingToday_shouldCountFromToday() {
        // given：今天、昨天、前天、大前天都有打卡
        givenCheckinDates(today, today.minusDays(1), today.minusDays(2), today.minusDays(3));

        // when
        CheckinStatusVO vo = checkinService.status(TASK_ID, CHILD_ID);

        // then
        assertThat(vo.getCheckedToday()).isTrue();
        assertThat(vo.getStreak()).isEqualTo(4);
        assertThat(vo.getTotalCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("今天还没打卡但昨天及之前连续：连续天数不应被误判为中断（关键边界）")
    void status_notCheckedTodayButContinuousUntilYesterday_shouldKeepStreak() {
        // given：昨天、前天、大前天连续，今天还没打卡
        givenCheckinDates(today.minusDays(1), today.minusDays(2), today.minusDays(3));

        // when
        CheckinStatusVO vo = checkinService.status(TASK_ID, CHILD_ID);

        // then：连续天数依然是 3，而不是被清零
        assertThat(vo.getCheckedToday()).isFalse();
        assertThat(vo.getStreak()).isEqualTo(3);
        assertThat(vo.getTotalCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("只有今天打过卡：连续天数为 1")
    void status_onlyToday_shouldBeOne() {
        // given
        givenCheckinDates(today);

        // when
        CheckinStatusVO vo = checkinService.status(TASK_ID, CHILD_ID);

        // then
        assertThat(vo.getCheckedToday()).isTrue();
        assertThat(vo.getStreak()).isEqualTo(1);
    }

    @Test
    @DisplayName("中间断了一天：连续天数只统计最近那一段")
    void status_withGapBeforeYesterday_shouldCountOnlyLatestSegment() {
        // given：昨天、前天连续，但大前天断档（再往前 4、5 天是连续的）
        givenCheckinDates(
                today.minusDays(1), today.minusDays(2),
                today.minusDays(4), today.minusDays(5));

        // when
        CheckinStatusVO vo = checkinService.status(TASK_ID, CHILD_ID);

        // then
        assertThat(vo.getCheckedToday()).isFalse();
        assertThat(vo.getStreak()).isEqualTo(2);
        assertThat(vo.getTotalCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("今天已打卡但昨天断档：连续天数只算今天这一天")
    void status_checkedTodayAfterGap_shouldCountOnlyToday() {
        // given：今天打卡，昨天没有，前天有
        givenCheckinDates(today, today.minusDays(2));

        // when
        CheckinStatusVO vo = checkinService.status(TASK_ID, CHILD_ID);

        // then
        assertThat(vo.getCheckedToday()).isTrue();
        assertThat(vo.getStreak()).isEqualTo(1);
        assertThat(vo.getTotalCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("中断后重新打卡：连续天数从 1 重新开始")
    void status_restartAfterLongGap_shouldResetToOne() {
        // given：很久以前打过卡（4、5 天前），今天重新开始
        givenCheckinDates(today, today.minusDays(4), today.minusDays(5));

        // when
        CheckinStatusVO vo = checkinService.status(TASK_ID, CHILD_ID);

        // then
        assertThat(vo.getCheckedToday()).isTrue();
        assertThat(vo.getStreak()).isEqualTo(1);
        assertThat(vo.getTotalCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("从未打卡：连续天数为 0，累计次数为 0")
    void status_noRecord_shouldBeAllZero() {
        // given
        givenCheckinDates();

        // when
        CheckinStatusVO vo = checkinService.status(TASK_ID, CHILD_ID);

        // then
        assertThat(vo.getCheckedToday()).isFalse();
        assertThat(vo.getStreak()).isZero();
        assertThat(vo.getTotalCount()).isZero();
    }

    @Test
    @DisplayName("同一天存在重复记录：按日期去重后统计，不会虚增打卡次数")
    void status_duplicateRecordsOnSameDay_shouldDeduplicateByDate() {
        // given：今天被插入了两条记录（并发或重试造成）
        givenCheckinDates(today, today, today.minusDays(1));

        // when
        CheckinStatusVO vo = checkinService.status(TASK_ID, CHILD_ID);

        // then
        assertThat(vo.getStreak()).isEqualTo(2);
        assertThat(vo.getTotalCount()).isEqualTo(2);
    }

    // ------------------------------------------------------------------
    // 打卡写入
    // ------------------------------------------------------------------

    @Test
    @DisplayName("今日首次打卡：写入今天的日期，备注为空串而非 null")
    void checkin_firstTimeOfToday_shouldInsertWithTodayDate() {
        // given
        when(checkinMapper.selectCount(any())).thenReturn(0L);
        when(checkinMapper.insert(any(Checkin.class))).thenReturn(1);

        // when
        Checkin result = checkinService.checkin(TASK_ID, CHILD_ID, null);

        // then
        ArgumentCaptor<Checkin> captor = ArgumentCaptor.forClass(Checkin.class);
        verify(checkinMapper).insert(captor.capture());
        Checkin saved = captor.getValue();
        assertThat(saved.getTaskId()).isEqualTo(TASK_ID);
        assertThat(saved.getChildId()).isEqualTo(CHILD_ID);
        assertThat(saved.getCheckinDate()).isEqualTo(today);
        assertThat(saved.getNote()).isEqualTo("");
        assertThat(result.getCheckinDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("今天已经打过卡：拒绝重复打卡，不写入新记录")
    void checkin_alreadyCheckedToday_shouldReject() {
        // given
        when(checkinMapper.selectCount(any())).thenReturn(1L);

        // when & then
        assertThatThrownBy(() -> checkinService.checkin(TASK_ID, CHILD_ID, "再来一次"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("今天已经打卡啦，明天继续加油");
        verify(checkinMapper, never()).insert(any(Checkin.class));
    }

    @Test
    @DisplayName("打卡备注原样保留：不为空串覆盖用户填写的内容")
    void checkin_withNote_shouldKeepNote() {
        // given
        when(checkinMapper.selectCount(any())).thenReturn(0L);
        when(checkinMapper.insert(any(Checkin.class))).thenReturn(1);

        // when
        checkinService.checkin(TASK_ID, CHILD_ID, "今天主动读了两页");

        // then
        ArgumentCaptor<Checkin> captor = ArgumentCaptor.forClass(Checkin.class);
        verify(checkinMapper).insert(captor.capture());
        assertThat(captor.getValue().getNote()).isEqualTo("今天主动读了两页");
    }

    // ------------------------------------------------------------------
    // 测试辅助
    // ------------------------------------------------------------------

    /** 让 baseMapper.selectList 返回指定日期的打卡记录 */
    private void givenCheckinDates(LocalDate... dates) {
        List<Checkin> records = new ArrayList<>();
        for (LocalDate date : dates) {
            Checkin c = new Checkin();
            c.setTaskId(TASK_ID);
            c.setChildId(CHILD_ID);
            c.setCheckinDate(date);
            c.setNote("");
            records.add(c);
        }
        when(checkinMapper.selectList(any())).thenReturn(records);
    }
}

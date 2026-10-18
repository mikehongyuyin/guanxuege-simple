package com.guanxuege.common.service;

import com.guanxuege.auth.mapper.ChildMapper;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.exception.ForbiddenException;
import com.guanxuege.grade.entity.Grade;
import com.guanxuege.grade.mapper.GradeMapper;
import com.guanxuege.learningplan.entity.Plan;
import com.guanxuege.learningplan.entity.PlanTask;
import com.guanxuege.learningplan.mapper.PlanMapper;
import com.guanxuege.learningplan.mapper.PlanTaskMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 数据归属校验（越权拦截）测试。
 * <p>
 * 系统的数据边界是「孩子核」：计划 / 任务 / 成绩都必须沿
 * task -> plan -> child -> parent_id 链路回溯到当前家长，
 * 任一层不属于当前用户就必须抛 {@link ForbiddenException}（HTTP 403）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class OwnershipServiceTest {

    private static final Long ME = 1L;

    private static final Long MY_CHILD = 100L;
    private static final Long OTHERS_CHILD = 200L;

    private static final Long MY_PLAN = 1000L;
    private static final Long MY_TASK = 5000L;
    private static final Long MY_GRADE = 9000L;

    @Mock
    private ChildMapper childMapper;

    @Mock
    private PlanMapper planMapper;

    @Mock
    private PlanTaskMapper planTaskMapper;

    @Mock
    private GradeMapper gradeMapper;

    @InjectMocks
    private OwnershipService ownershipService;

    // ------------------------------------------------------------------
    // 孩子
    // ------------------------------------------------------------------

    @Test
    @DisplayName("访问他人的孩子：抛 ForbiddenException（403），而不是业务异常")
    void requireChild_ofOthers_shouldThrowForbidden() {
        // given：孩子存在，但 parent_id 不是当前用户
        when(childMapper.selectCount(any())).thenReturn(0L);

        // when & then
        assertThatThrownBy(() -> ownershipService.requireChild(ME, OTHERS_CHILD))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("无权访问该孩子的数据");
    }

    @Test
    @DisplayName("访问自己的孩子：正常放行，不抛任何异常")
    void requireChild_ofMyself_shouldPass() {
        // given
        when(childMapper.selectCount(any())).thenReturn(1L);

        // when & then
        assertThatCode(() -> ownershipService.requireChild(ME, MY_CHILD))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("归属查询返回 null（异常兜底）：同样视为越权拦截，不放行")
    void requireChild_whenCountIsNull_shouldThrowForbidden() {
        // given
        when(childMapper.selectCount(any())).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> ownershipService.requireChild(ME, MY_CHILD))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("未登录（userId 为 null）：抛 BusinessException 要求登录")
    void requireChild_whenAnonymous_shouldThrowBusinessException() {
        assertThatThrownBy(() -> ownershipService.requireChild(null, MY_CHILD))
                .isInstanceOf(BusinessException.class)
                .hasMessage("未登录");
    }

    @Test
    @DisplayName("未指定孩子（childId 为 null）：抛 BusinessException 要求选择孩子")
    void requireChild_whenChildIdMissing_shouldThrowBusinessException() {
        assertThatThrownBy(() -> ownershipService.requireChild(ME, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请选择孩子");
    }

    // ------------------------------------------------------------------
    // 计划（plan -> child）
    // ------------------------------------------------------------------

    @Test
    @DisplayName("访问他人名下的计划：沿 plan -> child 链路上溯后被拦截")
    void requirePlan_ofOthers_shouldThrowForbidden() {
        // given：计划属于别人的孩子
        when(planMapper.selectById(MY_PLAN)).thenReturn(plan(MY_PLAN, OTHERS_CHILD));
        when(childMapper.selectCount(any())).thenReturn(0L);

        // when & then
        assertThatThrownBy(() -> ownershipService.requirePlan(ME, MY_PLAN))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("无权访问该孩子的数据");
    }

    @Test
    @DisplayName("访问自己的计划：返回计划实体且放行")
    void requirePlan_ofMyself_shouldReturnPlan() {
        // given
        when(planMapper.selectById(MY_PLAN)).thenReturn(plan(MY_PLAN, MY_CHILD));
        when(childMapper.selectCount(any())).thenReturn(1L);

        // when
        Plan result = ownershipService.requirePlan(ME, MY_PLAN);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(MY_PLAN);
        assertThat(result.getChildId()).isEqualTo(MY_CHILD);
    }

    @Test
    @DisplayName("计划不存在：抛 ForbiddenException，避免通过报错探测他人资源是否存在")
    void requirePlan_whenNotExist_shouldThrowForbidden() {
        // given
        when(planMapper.selectById(MY_PLAN)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> ownershipService.requirePlan(ME, MY_PLAN))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("计划不存在");
    }

    // ------------------------------------------------------------------
    // 任务（task -> plan -> child）
    // ------------------------------------------------------------------

    @Test
    @DisplayName("访问他人名下的任务：沿 task -> plan -> child 完整链路回溯后被拦截")
    void requireTask_ofOthers_shouldThrowForbidden() {
        // given：任务挂在自己的计划下，但计划的孩子属于别人
        when(planTaskMapper.selectById(MY_TASK)).thenReturn(task(MY_TASK, MY_PLAN));
        when(planMapper.selectById(MY_PLAN)).thenReturn(plan(MY_PLAN, OTHERS_CHILD));
        when(childMapper.selectCount(any())).thenReturn(0L);

        // when & then
        assertThatThrownBy(() -> ownershipService.requireTask(ME, MY_TASK))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("无权访问该孩子的数据");
    }

    @Test
    @DisplayName("任务所属计划不存在：拦截，不允许绕过归属校验")
    void requireTask_whenPlanMissing_shouldThrowForbidden() {
        // given
        when(planTaskMapper.selectById(MY_TASK)).thenReturn(task(MY_TASK, 9999L));
        when(planMapper.selectById(9999L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> ownershipService.requireTask(ME, MY_TASK))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("计划不存在");
    }

    @Test
    @DisplayName("任务不存在：抛 ForbiddenException")
    void requireTask_whenNotExist_shouldThrowForbidden() {
        // given
        when(planTaskMapper.selectById(MY_TASK)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> ownershipService.requireTask(ME, MY_TASK))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("任务不存在");
    }

    @Test
    @DisplayName("访问自己的任务：返回任务实体且放行")
    void requireTask_ofMyself_shouldReturnTask() {
        // given
        when(planTaskMapper.selectById(MY_TASK)).thenReturn(task(MY_TASK, MY_PLAN));
        when(planMapper.selectById(MY_PLAN)).thenReturn(plan(MY_PLAN, MY_CHILD));
        when(childMapper.selectCount(any())).thenReturn(1L);

        // when
        PlanTask result = ownershipService.requireTask(ME, MY_TASK);

        // then
        assertThat(result.getId()).isEqualTo(MY_TASK);
        assertThat(result.getPlanId()).isEqualTo(MY_PLAN);
    }

    // ------------------------------------------------------------------
    // 成绩（grade -> child）
    // ------------------------------------------------------------------

    @Test
    @DisplayName("访问他人孩子的成绩：抛 ForbiddenException")
    void requireGrade_ofOthers_shouldThrowForbidden() {
        // given
        when(gradeMapper.selectById(MY_GRADE)).thenReturn(grade(MY_GRADE, OTHERS_CHILD));
        when(childMapper.selectCount(any())).thenReturn(0L);

        // when & then
        assertThatThrownBy(() -> ownershipService.requireGrade(ME, MY_GRADE))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("无权访问该孩子的数据");
    }

    @Test
    @DisplayName("访问自己孩子的成绩：返回成绩实体且放行")
    void requireGrade_ofMyself_shouldReturnGrade() {
        // given
        when(gradeMapper.selectById(MY_GRADE)).thenReturn(grade(MY_GRADE, MY_CHILD));
        when(childMapper.selectCount(any())).thenReturn(1L);

        // when
        Grade result = ownershipService.requireGrade(ME, MY_GRADE);

        // then
        assertThat(result.getId()).isEqualTo(MY_GRADE);
        assertThat(result.getChildId()).isEqualTo(MY_CHILD);
    }

    @Test
    @DisplayName("成绩记录不存在：抛 ForbiddenException")
    void requireGrade_whenNotExist_shouldThrowForbidden() {
        // given
        when(gradeMapper.selectById(MY_GRADE)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> ownershipService.requireGrade(ME, MY_GRADE))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("成绩记录不存在");
    }

    // ------------------------------------------------------------------
    // 测试辅助
    // ------------------------------------------------------------------

    private static Plan plan(Long id, Long childId) {
        Plan plan = new Plan();
        plan.setId(id);
        plan.setChildId(childId);
        plan.setTitle("计划" + id);
        return plan;
    }

    private static PlanTask task(Long id, Long planId) {
        PlanTask task = new PlanTask();
        task.setId(id);
        task.setPlanId(planId);
        task.setTitle("任务" + id);
        return task;
    }

    private static Grade grade(Long id, Long childId) {
        Grade grade = new Grade();
        grade.setId(id);
        grade.setChildId(childId);
        grade.setSubject("数学");
        return grade;
    }
}

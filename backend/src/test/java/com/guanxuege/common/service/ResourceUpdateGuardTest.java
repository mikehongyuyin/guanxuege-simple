package com.guanxuege.common.service;

import com.guanxuege.auth.controller.ChildController;
import com.guanxuege.auth.entity.Child;
import com.guanxuege.auth.mapper.ChildMapper;
import com.guanxuege.auth.service.ChildService;
import com.guanxuege.common.exception.ForbiddenException;
import com.guanxuege.grade.controller.GradeController;
import com.guanxuege.grade.entity.Grade;
import com.guanxuege.grade.mapper.GradeMapper;
import com.guanxuege.grade.service.GradeService;
import com.guanxuege.learningplan.controller.PlanController;
import com.guanxuege.learningplan.controller.PlanTaskController;
import com.guanxuege.learningplan.entity.Plan;
import com.guanxuege.learningplan.entity.PlanTask;
import com.guanxuege.learningplan.mapper.PlanMapper;
import com.guanxuege.learningplan.mapper.PlanTaskMapper;
import com.guanxuege.learningplan.service.PlanService;
import com.guanxuege.learningplan.service.PlanTaskService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 「更新类接口不能把资源改挂到别的账号下」的守护测试。
 * <p>
 * 只校验「读」是不够的：攻击者可以用一次 PUT 把资源搬到别人的孩子 / 家长名下。
 * 这里用真实的 {@link OwnershipService}（依赖的 mapper 全部 mock）驱动各 Controller 的更新分支，
 * 验证越权重挂被拦截、合法重挂被放行、不可变归属字段被服务端强制改写。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ResourceUpdateGuardTest {

    private static final Long ME = 1L;

    private static final Long MY_CHILD = 100L;
    private static final Long MY_OTHER_CHILD = 101L;
    private static final Long OTHERS_CHILD = 200L;

    private static final Long PLAN_ID = 1000L;
    private static final Long TASK_ID = 5000L;
    private static final Long GRADE_ID = 9000L;

    @Mock
    private ChildMapper childMapper;

    @Mock
    private PlanMapper planMapper;

    @Mock
    private PlanTaskMapper planTaskMapper;

    @Mock
    private GradeMapper gradeMapper;

    /** 归属校验使用真实实现，其余外围服务用 mock */
    @InjectMocks
    private OwnershipService ownershipService;

    @Mock
    private PlanService planService;

    @Mock
    private PlanTaskService planTaskService;

    @Mock
    private GradeService gradeService;

    @Mock
    private ChildService childService;

    private PlanController planController;
    private PlanTaskController planTaskController;
    private GradeController gradeController;
    private ChildController childController;

    @BeforeEach
    void setUp() {
        planController = new PlanController();
        ReflectionTestUtils.setField(planController, "planService", planService);
        ReflectionTestUtils.setField(planController, "ownershipService", ownershipService);

        planTaskController = new PlanTaskController();
        ReflectionTestUtils.setField(planTaskController, "planTaskService", planTaskService);
        ReflectionTestUtils.setField(planTaskController, "ownershipService", ownershipService);

        gradeController = new GradeController();
        ReflectionTestUtils.setField(gradeController, "gradeService", gradeService);
        ReflectionTestUtils.setField(gradeController, "ownershipService", ownershipService);

        childController = new ChildController();
        ReflectionTestUtils.setField(childController, "childService", childService);
        ReflectionTestUtils.setField(childController, "ownershipService", ownershipService);
    }

    // ------------------------------------------------------------------
    // 计划
    // ------------------------------------------------------------------

    @Test
    @DisplayName("把计划改挂到别人的孩子名下：被 403 拦截，不执行更新")
    void updatePlan_moveToOthersChild_shouldBeForbidden() {
        // given：计划当前属于自己的孩子 100，攻击者想改成别人的孩子 200
        when(planMapper.selectById(PLAN_ID)).thenReturn(plan(PLAN_ID, MY_CHILD));
        // 第一次查询校验原归属（通过），第二次校验目标孩子（不通过）
        when(childMapper.selectCount(any())).thenReturn(1L, 0L);

        Plan payload = plan(PLAN_ID, OTHERS_CHILD);

        // when & then
        assertThatThrownBy(() -> planController.update(ME, payload))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("无权访问该孩子的数据");
        verify(planService, never()).updateById(any(Plan.class));
    }

    @Test
    @DisplayName("把计划改挂到自己的另一个孩子名下：放行，归属更新为目标孩子")
    void updatePlan_moveToMyOwnAnotherChild_shouldBeAllowed() {
        // given
        when(planMapper.selectById(PLAN_ID)).thenReturn(plan(PLAN_ID, MY_CHILD));
        when(childMapper.selectCount(any())).thenReturn(1L, 1L);

        // when
        planController.update(ME, plan(PLAN_ID, MY_OTHER_CHILD));

        // then
        ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
        verify(planService).updateById(captor.capture());
        assertThat(captor.getValue().getChildId()).isEqualTo(MY_OTHER_CHILD);
    }

    @Test
    @DisplayName("创建计划时挂到别人的孩子名下：被 403 拦截，不落库")
    void createPlan_withOthersChild_shouldBeForbidden() {
        // given
        when(childMapper.selectCount(any())).thenReturn(0L);

        // when & then
        assertThatThrownBy(() -> planController.create(ME, plan(null, OTHERS_CHILD)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("无权访问该孩子的数据");
        verify(planService, never()).save(any(Plan.class));
    }

    @Test
    @DisplayName("删除别人的计划：被 403 拦截，不执行删除")
    void deletePlan_ofOthers_shouldBeForbidden() {
        // given
        when(planMapper.selectById(PLAN_ID)).thenReturn(plan(PLAN_ID, OTHERS_CHILD));
        when(childMapper.selectCount(any())).thenReturn(0L);

        // when & then
        assertThatThrownBy(() -> planController.delete(ME, PLAN_ID))
                .isInstanceOf(ForbiddenException.class);
        verify(planService, never()).removeById(any(java.io.Serializable.class));
    }

    // ------------------------------------------------------------------
    // 任务
    // ------------------------------------------------------------------

    @Test
    @DisplayName("更新任务时把任务挪到别的计划：planId 被服务端强制改回原计划")
    void updateTask_moveToAnotherPlan_shouldBeForcedBackToOriginalPlan() {
        // given：任务原本挂在计划 1000 下
        when(planTaskMapper.selectById(TASK_ID)).thenReturn(task(TASK_ID, PLAN_ID));
        when(planMapper.selectById(PLAN_ID)).thenReturn(plan(PLAN_ID, MY_CHILD));
        when(childMapper.selectCount(any())).thenReturn(1L);

        PlanTask payload = task(TASK_ID, 9999L);
        payload.setTitle("被篡改的任务");

        // when
        planTaskController.update(ME, payload);

        // then：即使请求体里带了别的 planId，落库的仍然是原计划
        ArgumentCaptor<PlanTask> captor = ArgumentCaptor.forClass(PlanTask.class);
        verify(planTaskService).updateById(captor.capture());
        assertThat(captor.getValue().getPlanId()).isEqualTo(PLAN_ID);
        assertThat(captor.getValue().getTitle()).isEqualTo("被篡改的任务");
    }

    @Test
    @DisplayName("更新别人的任务：被 403 拦截，不执行更新")
    void updateTask_ofOthers_shouldBeForbidden() {
        // given：任务所属计划挂在别人的孩子下
        when(planTaskMapper.selectById(TASK_ID)).thenReturn(task(TASK_ID, PLAN_ID));
        when(planMapper.selectById(PLAN_ID)).thenReturn(plan(PLAN_ID, OTHERS_CHILD));
        when(childMapper.selectCount(any())).thenReturn(0L);

        // when & then
        assertThatThrownBy(() -> planTaskController.update(ME, task(TASK_ID, PLAN_ID)))
                .isInstanceOf(ForbiddenException.class);
        verify(planTaskService, never()).updateById(any(PlanTask.class));
    }

    // ------------------------------------------------------------------
    // 成绩
    // ------------------------------------------------------------------

    @Test
    @DisplayName("把成绩改挂到别人的孩子名下：被 403 拦截，不执行更新")
    void updateGrade_moveToOthersChild_shouldBeForbidden() {
        // given
        when(gradeMapper.selectById(GRADE_ID)).thenReturn(grade(GRADE_ID, MY_CHILD));
        when(childMapper.selectCount(any())).thenReturn(1L, 0L);

        // when & then
        assertThatThrownBy(() -> gradeController.update(ME, grade(GRADE_ID, OTHERS_CHILD)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("无权访问该孩子的数据");
        verify(gradeService, never()).updateById(any(Grade.class));
    }

    @Test
    @DisplayName("把成绩改挂到自己的另一个孩子名下：放行")
    void updateGrade_moveToMyOwnAnotherChild_shouldBeAllowed() {
        // given
        when(gradeMapper.selectById(GRADE_ID)).thenReturn(grade(GRADE_ID, MY_CHILD));
        when(childMapper.selectCount(any())).thenReturn(1L, 1L);

        // when
        gradeController.update(ME, grade(GRADE_ID, MY_OTHER_CHILD));

        // then
        ArgumentCaptor<Grade> captor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeService).updateById(captor.capture());
        assertThat(captor.getValue().getChildId()).isEqualTo(MY_OTHER_CHILD);
    }

    // ------------------------------------------------------------------
    // 孩子档案（家长归属不可被篡改）
    // ------------------------------------------------------------------

    @Test
    @DisplayName("修改他人孩子档案：被 403 拦截，不执行更新")
    void updateChild_ofOthers_shouldBeForbidden() {
        // given：该孩子在当前的 parent_id 下查不到（归属校验走 childMapper.selectCount）
        when(childMapper.selectCount(any())).thenReturn(0L);

        Child payload = child(MY_CHILD, ME);

        // when & then
        assertThatThrownBy(() -> childController.update(ME, payload))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("无权访问该孩子的数据");
        verify(childService, never()).updateById(any(Child.class));
    }

    @Test
    @DisplayName("修改自己孩子档案时传入他人 parentId：监护人被强制改回当前登录用户（杜绝过户）")
    void updateChild_withForeignParentId_shouldBeForcedBackToCurrentUser() {
        // given：孩子确实属于自己（校验通过），但请求体里试图把 parentId 改成 999
        when(childMapper.selectCount(any())).thenReturn(1L);

        Child payload = child(MY_CHILD, 999L);

        // when
        childController.update(ME, payload);

        // then：落库的 parentId 必须是当前登录用户
        ArgumentCaptor<Child> captor = ArgumentCaptor.forClass(Child.class);
        verify(childService).updateById(captor.capture());
        assertThat(captor.getValue().getParentId()).isEqualTo(ME);
        assertThat(captor.getValue().getParentId()).isNotEqualTo(999L);
    }

    // ------------------------------------------------------------------
    // 测试辅助
    // ------------------------------------------------------------------

    private static Plan plan(Long id, Long childId) {
        Plan plan = new Plan();
        plan.setId(id);
        plan.setChildId(childId);
        plan.setTitle("计划");
        return plan;
    }

    private static PlanTask task(Long id, Long planId) {
        PlanTask task = new PlanTask();
        task.setId(id);
        task.setPlanId(planId);
        task.setTitle("任务");
        return task;
    }

    private static Grade grade(Long id, Long childId) {
        Grade grade = new Grade();
        grade.setId(id);
        grade.setChildId(childId);
        grade.setSubject("数学");
        return grade;
    }

    private static Child child(Long id, Long parentId) {
        Child child = new Child();
        child.setId(id);
        child.setParentId(parentId);
        child.setName("小明");
        return child;
    }
}

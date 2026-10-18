package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.guanxuege.common.MybatisPlusSqlFixture;
import com.guanxuege.community.entity.Circle;
import com.guanxuege.community.entity.UserCircle;
import com.guanxuege.community.mapper.CircleMapper;
import com.guanxuege.community.mapper.UserCircleMapper;
import com.guanxuege.community.vo.CircleVO;
import org.junit.jupiter.api.BeforeAll;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 圈子的社区维度隔离测试。
 * <p>
 * 育儿社区与心声社区共用一张 gx_circle 表，靠 community 字段区分。
 * 一旦查询条件漏掉 community，用户就会在育儿社区里看到「副业交流」这类圈
 * ——功能上看似只是多了几个圈子，实际是两个产品的内容互相串台。
 * 同时老接口 listAll(userId) 必须保持「不加过滤」的历史行为，避免误伤既有调用方。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class CircleServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private CircleMapper circleMapper;

    @Mock
    private UserCircleMapper userCircleMapper;

    @InjectMocks
    private CircleServiceImpl circleService;

    @BeforeAll
    static void initMetadata() {
        MybatisPlusSqlFixture.initTableInfo(Circle.class);
    }

    @BeforeEach
    void bindBaseMapper() {
        ReflectionTestUtils.setField(circleService, "baseMapper", circleMapper);
    }

    // ------------------------------------------------------------------
    // 1. 社区维度过滤
    // ------------------------------------------------------------------

    @Test
    @DisplayName("查询心声社区的圈子：SQL 条件里必须带 community = voice")
    void listAll_voice_shouldFilterByCommunity() {
        // given
        stubCircles(circle(1L, "副业交流", "voice"), circle(2L, "职场吐槽", "voice"));

        // when
        List<CircleVO> vos = circleService.listAll(null, "voice");

        // then
        assertThat(vos).hasSize(2);
        Wrapper<Circle> wrapper = capturedWrapper();
        assertThat(sqlWhereOf(wrapper)).contains("community");
        assertThat(paramValuesOf(wrapper)).contains("voice");
    }

    @Test
    @DisplayName("查询育儿社区的圈子：过滤值为 parenting")
    void listAll_parenting_shouldFilterByCommunity() {
        // given
        stubCircles(circle(3L, "习惯养成", "parenting"));

        // when
        circleService.listAll(null, "parenting");

        // then
        Wrapper<Circle> wrapper = capturedWrapper();
        assertThat(sqlWhereOf(wrapper)).contains("community");
        assertThat(paramValuesOf(wrapper)).contains("parenting");
    }

    @Test
    @DisplayName("传入非法 community（如 xxx）：归一化回 parenting，绝不能拼出空条件把两个社区的内容一起查出来")
    void listAll_illegalCommunity_shouldFallbackToParenting() {
        // given
        stubCircles(circle(3L, "习惯养成", "parenting"));

        // when
        circleService.listAll(null, "xxx");

        // then
        assertThat(paramValuesOf(capturedWrapper())).contains("parenting");
    }

    @Test
    @DisplayName("community 大小写与首尾空格容错：' VOICE ' 仍归一化为 voice")
    void listAll_communityWithCaseAndSpace_shouldNormalize() {
        // given
        stubCircles(circle(1L, "副业交流", "voice"));

        // when
        circleService.listAll(null, " VOICE ");

        // then
        assertThat(paramValuesOf(capturedWrapper())).contains("voice");
    }

    @Test
    @DisplayName("不传 community：不加 community 过滤条件（保持老接口语义，查询全部圈子）")
    void listAll_withoutCommunity_shouldNotFilter() {
        // given
        stubCircles(circle(1L, "副业交流", "voice"), circle(3L, "习惯养成", "parenting"));

        // when
        List<CircleVO> vos = circleService.listAll(null, null);

        // then：未指定社区时两个社区的圈子一起返回，且 SQL 里没有 community 条件
        assertThat(vos).hasSize(2);
        assertThat(paramValuesOf(capturedWrapper())).isEmpty();
    }

    @Test
    @DisplayName("单参重载 listAll(userId)：等价于不传 community，行为向后兼容")
    void listAll_singleArgOverload_shouldNotFilter() {
        // given
        stubCircles(circle(1L, "副业交流", "voice"), circle(3L, "习惯养成", "parenting"));

        // when
        List<CircleVO> vos = circleService.listAll(null);

        // then
        assertThat(vos).hasSize(2);
        assertThat(paramValuesOf(capturedWrapper())).isEmpty();
    }

    // ------------------------------------------------------------------
    // 2. joined 标记与查询次数
    // ------------------------------------------------------------------

    @Test
    @DisplayName("已加入的圈子 joined 为 true，未加入的为 false")
    void listAll_withUserId_shouldMarkJoined() {
        // given：用户只加入了 2 号圈子
        stubCircles(circle(1L, "副业交流", "voice"), circle(2L, "职场吐槽", "voice"));
        when(userCircleMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(userCircle(USER_ID, 2L)));

        // when
        List<CircleVO> vos = circleService.listAll(USER_ID, "voice");

        // then
        assertThat(vos).hasSize(2);
        assertThat(vos.get(0).getJoined()).isFalse();
        assertThat(vos.get(1).getJoined()).isTrue();
        // 加入状态是一次批量查询后回填的，不是每个圈子查一次
        verify(userCircleMapper).selectList(any(Wrapper.class));
    }

    @Test
    @DisplayName("未登录游客：全部 joined 为 false，且一次都不查用户-圈子关系表")
    void listAll_whenAnonymous_shouldAllBeNotJoined() {
        // given
        stubCircles(circle(1L, "副业交流", "voice"), circle(2L, "职场吐槽", "voice"));

        // when
        List<CircleVO> vos = circleService.listAll(null, "voice");

        // then
        assertThat(vos).hasSize(2);
        assertThat(vos).allSatisfy(vo -> assertThat(vo.getJoined()).isFalse());
        verify(userCircleMapper, never()).selectList(any(Wrapper.class));
    }

    // ------------------------------------------------------------------
    // 测试辅助
    // ------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubCircles(Circle... circles) {
        when(circleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(circles));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Wrapper<Circle> capturedWrapper() {
        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(circleMapper).selectList(captor.capture());
        return captor.getValue();
    }

    /** 取出 Wrapper 的 WHERE 片段，用于断言 community 条件确实拼进了 SQL */
    private static String sqlWhereOf(Wrapper<Circle> wrapper) {
        return wrapper.getCustomSqlSegment();
    }

    /**
     * 取出 Wrapper 的占位符参数值，用于断言过滤值被正确归一化。
     * 先调一次 getTargetSql() 触发列名解析——LambdaWrapper 是懒解析的。
     */
    @SuppressWarnings("unchecked")
    private static List<Object> paramValuesOf(Wrapper<Circle> wrapper) {
        wrapper.getTargetSql();
        Map<String, Object> params =
                (Map<String, Object>) ReflectionTestUtils.invokeMethod(wrapper, "getParamNameValuePairs");
        return List.copyOf(params.values());
    }

    private static Circle circle(Long id, String name, String community) {
        Circle c = new Circle();
        c.setId(id);
        c.setName(name);
        c.setCommunity(community);
        c.setSort(id.intValue());
        return c;
    }

    private static UserCircle userCircle(Long userId, Long circleId) {
        UserCircle uc = new UserCircle();
        uc.setUserId(userId);
        uc.setCircleId(circleId);
        return uc;
    }
}

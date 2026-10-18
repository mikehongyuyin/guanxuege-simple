package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.MybatisPlusSqlFixture;
import com.guanxuege.common.constants.CommunityType;
import com.guanxuege.common.dto.UserBrief;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.mapper.CircleMapper;
import com.guanxuege.community.mapper.PostLikeMapper;
import com.guanxuege.community.mapper.PostMapper;
import com.guanxuege.community.service.MentionService;
import com.guanxuege.community.vo.PostVO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 帖子的社区维度隔离测试。
 * <p>
 * 育儿社区（parenting）与心声社区（voice）共用 gx_post 表，靠 community 字段区分，
 * 目的是不为新社区复制整套表结构与业务逻辑（点赞幂等、内容护栏、@提及只写一份）。
 * 代价是：任何一条查帖子列表的 SQL 漏掉 community 过滤，两个社区的内容就会互相串台。
 * 这里用 ArgumentCaptor 直接断言查询条件，把这条不变量钉死。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class PostCommunityIsolationTest {

    private static final Long AUTHOR_ID = 10L;
    private static final Long CIRCLE_ID = 5L;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private CircleMapper circleMapper;

    @Mock
    private UserService userService;

    @Mock
    private MentionService mentionService;

    @InjectMocks
    private PostServiceImpl postService;

    @BeforeAll
    static void initMetadata() {
        MybatisPlusSqlFixture.initTableInfo(Post.class);
    }

    @BeforeEach
    void bindBaseMapper() {
        ReflectionTestUtils.setField(postService, "baseMapper", postMapper);
    }

    // ------------------------------------------------------------------
    // 1. 列表查询的社区过滤
    // ------------------------------------------------------------------

    @Test
    @DisplayName("查询心声社区帖子列表：SQL 条件必须同时带 status 与 community = voice")
    void listByTab_voice_shouldFilterByCommunity() {
        // given
        stubPage(List.of(post(1L, "voice")));
        stubEnrich();

        // when
        List<PostVO> vos = postService.listByTab("latest", 1, 10, null, null, CommunityType.VOICE);

        // then
        assertThat(vos).hasSize(1);
        Wrapper<Post> wrapper = capturedQueryWrapper();
        assertThat(sqlWhereOf(wrapper)).contains("community");
        assertThat(paramValuesOf(wrapper)).contains("voice", 1);
    }

    @Test
    @DisplayName("查询育儿社区帖子列表：过滤值为 parenting")
    void listByTab_parenting_shouldFilterByCommunity() {
        // given
        stubPage(List.of(post(1L, "parenting")));
        stubEnrich();

        // when
        postService.listByTab("latest", 1, 10, null, null, CommunityType.PARENTING);

        // then
        assertThat(paramValuesOf(capturedQueryWrapper())).contains("parenting");
    }

    @Test
    @DisplayName("传入非法 community（如 xxx）：归一化回 parenting，绝不能漏掉过滤条件把两个社区的内容混在一起")
    void listByTab_illegalCommunity_shouldFallbackToParenting() {
        // given
        stubPage(List.of(post(1L, "parenting")));
        stubEnrich();

        // when
        postService.listByTab("latest", 1, 10, null, null, "xxx");

        // then
        assertThat(paramValuesOf(capturedQueryWrapper())).contains("parenting");
    }

    @Test
    @DisplayName("community 传 null：默认育儿社区，保证帖子不会因为缺字段而「消失」")
    void listByTab_nullCommunity_shouldDefaultToParenting() {
        // given
        stubPage(List.of(post(1L, "parenting")));
        stubEnrich();

        // when
        postService.listByTab("latest", 1, 10, null, null, null);

        // then
        assertThat(paramValuesOf(capturedQueryWrapper())).contains("parenting");
    }

    @Test
    @DisplayName("五参重载 listByTab：等价于 parenting，老调用方行为不变")
    void listByTab_fiveArgOverload_shouldDefaultToParenting() {
        // given
        stubPage(List.of(post(1L, "parenting")));
        stubEnrich();

        // when
        postService.listByTab("latest", 1, 10, null, null);

        // then
        assertThat(paramValuesOf(capturedQueryWrapper())).contains("parenting");
    }

    @Test
    @DisplayName("社区过滤与圈子过滤叠加：查心声社区某圈子时，circle_id 与 community 两个条件必须同时生效")
    void listByTab_withCircleId_shouldApplyBothFilters() {
        // given
        stubPage(List.of(post(1L, "voice")));
        stubEnrich();

        // when
        postService.listByTab("latest", 1, 10, CIRCLE_ID, null, CommunityType.VOICE);

        // then
        Wrapper<Post> wrapper = capturedQueryWrapper();
        assertThat(sqlWhereOf(wrapper)).contains("community").contains("circle_id");
        assertThat(paramValuesOf(wrapper)).contains("voice", CIRCLE_ID);
    }

    @Test
    @DisplayName("两个社区共用同一条查询：SQL 骨架完全一致，只有 community 参数不同（不为新社区复制逻辑）")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void listByTab_differentCommunity_shouldReuseSameQueryShape() {
        // given
        stubEnrich();

        // when：先查心声社区，再查育儿社区
        stubPage(List.of(post(1L, "voice")));
        postService.listByTab("latest", 1, 10, null, null, CommunityType.VOICE);
        stubPage(List.of(post(2L, "parenting")));
        postService.listByTab("latest", 1, 10, null, null, CommunityType.PARENTING);

        // then：两条 SQL 骨架一致（占位符已归一化为 ?），只有过滤参数不同
        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper, times(2)).selectPage(any(Page.class), captor.capture());
        Wrapper<Post> voiceQuery = captor.getAllValues().get(0);
        Wrapper<Post> parentingQuery = captor.getAllValues().get(1);

        assertThat(voiceQuery.getTargetSql()).isEqualTo(parentingQuery.getTargetSql());
        assertThat(paramValuesOf(voiceQuery)).contains("voice");
        assertThat(paramValuesOf(parentingQuery)).contains("parenting");
    }

    // ------------------------------------------------------------------
    // 2. 排序与空结果
    // ------------------------------------------------------------------

    @Test
    @DisplayName("latest 按创建时间倒序：加上社区过滤后排序规则不受影响")
    void listByTab_latest_shouldOrderByCreateTimeDesc() {
        // given
        stubPage(List.of(post(1L, "voice")));
        stubEnrich();

        // when
        postService.listByTab("latest", 1, 10, null, null, CommunityType.VOICE);
        Wrapper<Post> latest = capturedQueryWrapper();

        // then
        assertThat(latest.getTargetSql()).contains("create_time").contains("DESC");
        // 且社区过滤依然在
        assertThat(paramValuesOf(latest)).contains("voice");
    }

    @Test
    @DisplayName("recommend 按热度倒序：排序与社区过滤可以叠加生效")
    void listByTab_recommend_shouldOrderByHotScoreDesc() {
        // given
        stubPage(List.of(post(1L, "voice")));
        stubEnrich();

        // when
        postService.listByTab("recommend", 1, 10, null, null, CommunityType.VOICE);
        Wrapper<Post> recommend = capturedQueryWrapper();

        // then
        assertThat(recommend.getTargetSql()).contains("hot_score").contains("DESC");
        assertThat(paramValuesOf(recommend)).contains("voice");
    }

    @Test
    @DisplayName("查询无结果：直接返回空列表，不再查询作者与提及（避免空转）")
    void listByTab_emptyResult_shouldSkipEnrichQueries() {
        // given
        stubPage(List.of());

        // when
        List<PostVO> vos = postService.listByTab("latest", 1, 10, null, null, CommunityType.VOICE);

        // then
        assertThat(vos).isEmpty();
        verify(userService, never()).getBrief(anyLong());
        verify(mentionService, never()).mapByBizIds(anyString(), anyCollection());
    }

    // ------------------------------------------------------------------
    // 3. 写入侧：帖子归属社区
    // ------------------------------------------------------------------

    @Test
    @DisplayName("在心声社区发帖：community 原样落库为 voice")
    void create_inVoiceCommunity_shouldPersistCommunity() {
        // given
        Post post = new Post();
        post.setTitle("副业第一笔收入");
        post.setContent("今天开始练字");
        post.setCommunity(CommunityType.VOICE);
        stubInsertAndReload(post);

        // when
        postService.create(post, AUTHOR_ID);

        // then
        assertThat(capturedInsertedPost().getCommunity()).isEqualTo("voice");
    }

    @Test
    @DisplayName("发帖时 community 为 null：默认育儿社区，帖子不会变成无归属的游离数据")
    void create_withoutCommunity_shouldDefaultToParenting() {
        // given
        Post post = new Post();
        post.setTitle("如何培养阅读习惯");
        post.setContent("今天开始练字");
        post.setCommunity(null);
        stubInsertAndReload(post);

        // when
        postService.create(post, AUTHOR_ID);

        // then
        assertThat(capturedInsertedPost().getCommunity()).isEqualTo("parenting");
    }

    @Test
    @DisplayName("发帖时 community 为非法值：归一化回育儿社区，脏数据不会导致帖子在两个社区列表里都查不到")
    void create_withIllegalCommunity_shouldNormalizeToParenting() {
        // given
        Post post = new Post();
        post.setTitle("如何培养阅读习惯");
        post.setContent("今天开始练字");
        post.setCommunity("unknown-community");
        stubInsertAndReload(post);

        // when
        postService.create(post, AUTHOR_ID);

        // then
        assertThat(capturedInsertedPost().getCommunity()).isEqualTo("parenting");
    }

    // ------------------------------------------------------------------
    // 测试辅助
    // ------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubPage(List<Post> posts) {
        when(postMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(inv -> {
            Page<Post> page = inv.getArgument(0);
            page.setRecords(new ArrayList<>(posts));
            page.setTotal(posts.size());
            return page;
        });
    }

    /** 列表富化所需的最小桩：作者信息与批量提及 */
    private void stubEnrich() {
        when(userService.getBrief(anyLong())).thenAnswer(inv -> {
            UserBrief brief = new UserBrief();
            brief.setId(inv.getArgument(0));
            brief.setNickname("家长" + inv.getArgument(0));
            return brief;
        });
        when(mentionService.mapByBizIds(anyString(), anyCollection())).thenReturn(Map.of());
    }

    private void stubInsertAndReload(Post post) {
        when(postMapper.insert(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(2001L);
            return 1;
        });
        when(postMapper.selectById(2001L)).thenReturn(post);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Wrapper<Post> capturedQueryWrapper() {
        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).selectPage(any(Page.class), captor.capture());
        return captor.getValue();
    }

    private Post capturedInsertedPost() {
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).insert(captor.capture());
        return captor.getValue();
    }

    private static String sqlWhereOf(Wrapper<Post> wrapper) {
        return wrapper.getCustomSqlSegment();
    }

    /**
     * 取出占位符参数值，用于断言 community 的过滤值。
     * 先调一次 getTargetSql() 触发列名解析——LambdaWrapper 是懒解析的。
     */
    @SuppressWarnings("unchecked")
    private static List<Object> paramValuesOf(Wrapper<Post> wrapper) {
        wrapper.getTargetSql();
        Map<String, Object> params =
                (Map<String, Object>) ReflectionTestUtils.invokeMethod(wrapper, "getParamNameValuePairs");
        return List.copyOf(params.values());
    }

    private static Post post(Long id, String community) {
        Post p = new Post();
        p.setId(id);
        p.setAuthorId(AUTHOR_ID);
        p.setCommunity(community);
        p.setTitle("标题" + id);
        p.setContent("正文" + id);
        p.setLikeCount(0);
        p.setHotScore(1);
        p.setViewCount(0);
        p.setCommentCount(0);
        p.setStatus(1);
        p.setAnxietyFlag(0);
        return p;
    }
}

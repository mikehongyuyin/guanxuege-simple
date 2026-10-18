package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guanxuege.auth.service.UserService;
import com.guanxuege.common.dto.UserBrief;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.common.exception.ForbiddenException;
import com.guanxuege.community.entity.Post;
import com.guanxuege.community.entity.PostLike;
import com.guanxuege.community.mapper.CircleMapper;
import com.guanxuege.community.mapper.PostLikeMapper;
import com.guanxuege.community.mapper.PostMapper;
import com.guanxuege.community.service.MentionService;
import com.guanxuege.community.vo.PostVO;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 社区帖子核心不变量测试。
 * <p>
 * 重点守护两条规则：
 * 1. 点赞是「幂等 toggle」而非累加计数——同一用户对同一帖反复点击，点赞数只在 N 与 N+1 之间来回；
 * 2. 内容安全护栏：违禁词拦截、焦虑词放行但标记（绝不能误伤成拦截）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class PostServiceImplTest {

    private static final Long POST_ID = 1001L;
    private static final Long USER_A = 10L;
    private static final Long USER_B = 20L;

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

    @BeforeEach
    void bindBaseMapper() {
        // baseMapper 定义在 MyBatis-Plus 的 ServiceImpl 父类中，且其泛型被擦除为 BaseMapper，
        // 仅依赖 @InjectMocks 可能与其它 BaseMapper 子接口混淆，这里显式绑定被测对象的 mapper。
        ReflectionTestUtils.setField(postService, "baseMapper", postMapper);
    }

    // ------------------------------------------------------------------
    // 1. 点赞幂等（toggle）
    // ------------------------------------------------------------------

    @Test
    @DisplayName("首次点赞：点赞数 +1，并向关系表写入一条点赞记录")
    void like_firstTime_shouldIncreaseCountAndInsertRelation() {
        // given：帖子当前 5 个赞，用户 A 尚未点赞
        Post post = post(POST_ID, 5, 12);
        when(postMapper.selectById(POST_ID)).thenReturn(post);
        when(postLikeMapper.findRawId(POST_ID, USER_A)).thenReturn(null);
        when(postLikeMapper.insert(any(PostLike.class))).thenReturn(1);
        bindAtomicCounter(post);

        // when
        Integer result = postService.like(POST_ID, USER_A);

        // then
        assertThat(result).isEqualTo(6);

        ArgumentCaptor<PostLike> likeCaptor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeMapper).insert(likeCaptor.capture());
        assertThat(likeCaptor.getValue().getPostId()).isEqualTo(POST_ID);
        assertThat(likeCaptor.getValue().getUserId()).isEqualTo(USER_A);

        // 计数必须走数据库原子自增，而非内存里的「读-改-写」
        ArgumentCaptor<Wrapper<Post>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).update(isNull(), wrapperCaptor.capture());
        assertThat(sqlSetOf(wrapperCaptor.getValue()))
                .contains("like_count = like_count + 1")
                .contains("hot_score = hot_score + 1");

        verify(postLikeMapper, never()).deleteById(any(Serializable.class));
    }

    @Test
    @DisplayName("再次点赞（同一用户同一帖子）：取消点赞，点赞数回退 -1 且关系表记录被删除")
    void like_secondTime_shouldCancelAndDecreaseCount() {
        // given：关系表中已存在用户 A 的点赞记录（id=77）
        Post post = post(POST_ID, 5, 12);
        when(postMapper.selectById(POST_ID)).thenReturn(post);
        when(postLikeMapper.findRawId(POST_ID, USER_A)).thenReturn(77L);
        when(postLikeMapper.deleteById(77L)).thenReturn(1);
        bindAtomicCounter(post);

        // when
        Integer result = postService.like(POST_ID, USER_A);

        // then
        assertThat(result).isEqualTo(4);
        verify(postLikeMapper).deleteById(77L);
        verify(postLikeMapper, never()).insert(any(PostLike.class));

        // 计数同样走原子自减，且被 gt(like_count, 0) 兜底不会减成负数
        ArgumentCaptor<Wrapper<Post>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).update(isNull(), wrapperCaptor.capture());
        assertThat(sqlSetOf(wrapperCaptor.getValue()))
                .contains("like_count = like_count - 1")
                .contains("hot_score = hot_score - 1");
    }

    @Test
    @DisplayName("模拟用户连点 10 次：点赞数只在 N 与 N+1 之间来回，绝不单调累加")
    void like_repeatedly_shouldOnlyToggleBetweenNandNPlusOne() {
        // given：用内存态模拟 gx_post_like 唯一约束（post_id, user_id）
        Post post = post(POST_ID, 5, 12);
        when(postMapper.selectById(POST_ID)).thenReturn(post);
        FakeLikeTable likeTable = new FakeLikeTable();
        likeTable.bindTo(postLikeMapper);
        bindAtomicCounter(post);

        // when：连续调用 10 次
        Set<Integer> observedCounts = new LinkedHashSet<>();
        for (int i = 0; i < 10; i++) {
            observedCounts.add(postService.like(POST_ID, USER_A));
        }

        // then：观测到的点赞数只有 5 和 6 两个值，且偶数次调用后回到初始值
        assertThat(observedCounts).containsExactlyInAnyOrder(5, 6);
        assertThat(post.getLikeCount()).isEqualTo(5);
        assertThat(likeTable.size()).isZero();
        verify(postLikeMapper, times(5)).insert(any(PostLike.class));
        verify(postLikeMapper, times(5)).deleteById(any(Serializable.class));
    }

    @Test
    @DisplayName("点赞数不会被压成负数：计数已为 0 时再次 toggle 取消，仍停在 0")
    void like_cancelWhenCountIsZero_shouldClampAtZero() {
        // given：历史脏数据导致计数为 0，但关系表里仍有记录
        Post post = post(POST_ID, 0, 0);
        when(postMapper.selectById(POST_ID)).thenReturn(post);
        when(postLikeMapper.findRawId(POST_ID, USER_A)).thenReturn(88L);
        when(postLikeMapper.deleteById(88L)).thenReturn(1);
        bindAtomicCounter(post);

        // when
        Integer result = postService.like(POST_ID, USER_A);

        // then
        assertThat(result).isZero();
        assertThat(post.getHotScore()).isZero();
    }

    @Test
    @DisplayName("不同用户给同一帖子点赞：各自独立生效，互不影响")
    void like_byDifferentUsers_shouldBeIndependent() {
        // given
        Post post = post(POST_ID, 5, 12);
        when(postMapper.selectById(POST_ID)).thenReturn(post);
        FakeLikeTable likeTable = new FakeLikeTable();
        likeTable.bindTo(postLikeMapper);
        bindAtomicCounter(post);

        // when：A 点赞 -> B 点赞 -> A 取消点赞
        Integer afterA = postService.like(POST_ID, USER_A);
        Integer afterB = postService.like(POST_ID, USER_B);
        Integer afterACancel = postService.like(POST_ID, USER_A);

        // then
        assertThat(afterA).isEqualTo(6);
        assertThat(afterB).isEqualTo(7);
        assertThat(afterACancel).isEqualTo(6);
        // A 的点赞关系已删除，B 的仍然存在
        assertThat(likeTable.size()).isEqualTo(1);
        assertThat(likeTable.contains(POST_ID, USER_B)).isTrue();
        assertThat(likeTable.contains(POST_ID, USER_A)).isFalse();
    }

    @Test
    @DisplayName("帖子不存在时点赞：抛出业务异常，且不写关系表")
    void like_postNotExist_shouldThrowBusinessException() {
        // given
        when(postMapper.selectById(POST_ID)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> postService.like(POST_ID, USER_A))
                .isInstanceOf(BusinessException.class)
                .hasMessage("帖子不存在");
        verify(postLikeMapper, never()).insert(any(PostLike.class));
        verify(postMapper, never()).updateById(any(Post.class));
    }

    // ------------------------------------------------------------------
    // 2. liked 字段回填
    // ------------------------------------------------------------------

    @Test
    @DisplayName("详情返回：当前用户已点赞时 liked 为 true")
    void detail_whenCurrentUserLiked_shouldReturnLikedTrue() {
        // given
        Post post = post(POST_ID, 3, 3);
        when(postMapper.selectById(POST_ID)).thenReturn(post);
        when(postLikeMapper.findRawId(POST_ID, USER_A)).thenReturn(77L);

        // when
        PostVO vo = postService.getVo(POST_ID, USER_A);

        // then
        assertThat(vo).isNotNull();
        assertThat(vo.getLiked()).isTrue();
        assertThat(vo.getLikeCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("详情返回：当前用户未点赞时 liked 为 false")
    void detail_whenCurrentUserNotLiked_shouldReturnLikedFalse() {
        // given
        Post post = post(POST_ID, 3, 3);
        when(postMapper.selectById(POST_ID)).thenReturn(post);
        when(postLikeMapper.findRawId(POST_ID, USER_B)).thenReturn(null);

        // when
        PostVO vo = postService.getVo(POST_ID, USER_B);

        // then
        assertThat(vo.getLiked()).isFalse();
    }

    @Test
    @DisplayName("未登录用户查看详情：liked 为 false，且不查询点赞关系")
    void detail_whenAnonymous_shouldReturnLikedFalseWithoutQueryingLikeTable() {
        // given
        Post post = post(POST_ID, 3, 3);
        when(postMapper.selectById(POST_ID)).thenReturn(post);

        // when
        PostVO vo = postService.getVo(POST_ID, null);

        // then
        assertThat(vo.getLiked()).isFalse();
        verify(postLikeMapper, never()).findRawId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("列表返回：liked 按当前用户批量回填，只查一次关系表（避免 N+1）")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void list_shouldBackfillLikedInBatch() {
        // given：两篇帖子，当前用户只给第 1 篇点过赞
        Post p1 = post(1L, 2, 2, 101L);
        Post p2 = post(2L, 9, 9, 102L);
        stubPage(List.of(p1, p2));
        when(postLikeMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(likeRow(1L, USER_A)));
        when(userService.getBrief(anyLong())).thenAnswer(inv -> brief(inv.getArgument(0)));

        // when
        List<PostVO> vos = postService.listByTab("latest", 1, 10, null, USER_A);

        // then
        assertThat(vos).hasSize(2);
        assertThat(vos.get(0).getLiked()).isTrue();
        assertThat(vos.get(1).getLiked()).isFalse();
        // 作者信息回填正常
        assertThat(vos.get(0).getAuthorNickname()).isEqualTo("家长101");
        // 批量回填的核心：整页只查一次点赞关系，而不是每篇一次
        verify(postLikeMapper, times(1)).selectList(any(Wrapper.class));
        verify(postLikeMapper, never()).findRawId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("列表返回：未登录用户看到的所有帖子 liked 均为 false")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void list_whenAnonymous_shouldReturnAllLikedFalse() {
        // given
        Post p1 = post(1L, 2, 2, 101L);
        Post p2 = post(2L, 9, 9, 102L);
        stubPage(List.of(p1, p2));
        when(userService.getBrief(anyLong())).thenAnswer(inv -> brief(inv.getArgument(0)));

        // when
        List<PostVO> vos = postService.listByTab("recommend", 1, 10, null, null);

        // then
        assertThat(vos).hasSize(2);
        assertThat(vos).allSatisfy(vo -> assertThat(vo.getLiked()).isFalse());
        verify(postLikeMapper, never()).selectList(any(Wrapper.class));
    }

    @Test
    @DisplayName("列表返回：取作者信息失败时降级为不带作者信息，不影响帖子列表本身")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void list_whenAuthorServiceFails_shouldDegradeGracefully() {
        // given
        Post p1 = post(1L, 2, 2, 101L);
        stubPage(List.of(p1));
        when(postLikeMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(userService.getBrief(anyLong())).thenThrow(new RuntimeException("用户服务不可用"));

        // when
        List<PostVO> vos = postService.listByTab("latest", 1, 10, null, USER_A);

        // then
        assertThat(vos).hasSize(1);
        assertThat(vos.get(0).getAuthorNickname()).isNull();
        assertThat(vos.get(0).getLiked()).isFalse();
    }

    // ------------------------------------------------------------------
    // 3. 内容安全护栏在发帖链路上的落库效果
    // ------------------------------------------------------------------

    @Test
    @DisplayName("发帖命中违禁词：内容被拦截，绝不落库")
    void create_withBannedWord_shouldRejectAndNeverInsert() {
        // given
        Post post = new Post();
        post.setTitle("辅导作业太崩溃了");
        post.setContent("真的有去死的念头");

        // when & then
        assertThatThrownBy(() -> postService.create(post, USER_A))
                .isInstanceOf(BusinessException.class)
                .hasMessage("内容包含不当词汇，请修改后再发布");
        verify(postMapper, never()).insert(any(Post.class));
    }

    @Test
    @DisplayName("发帖命中焦虑词：内容放行但被标记，并返回温柔提醒文案（不是拦截）")
    void create_withAnxietyWord_shouldPersistAndFlagWithWarning() {
        // given
        Post post = new Post();
        post.setTitle("大家都是怎么鸡娃的？");
        post.setContent("想请教下时间安排，孩子三年级");
        when(postMapper.insert(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(POST_ID);
            return 1;
        });
        when(postMapper.selectById(POST_ID)).thenReturn(post);

        // when
        PostVO vo = postService.create(post, USER_A);

        // then：帖子照常发布（放行）
        verify(postMapper).insert(any(Post.class));
        assertThat(vo).isNotNull();
        // 但被打上焦虑标记，并带上温柔文案
        assertThat(vo.getAnxietyFlag()).isEqualTo(1);
        assertThat(vo.getWarning()).contains("鸡娃")
                .contains("每个孩子都有自己的节奏");
        // 新建帖子的初始计数必须是 0，不能被调用方传入的值污染
        assertThat(vo.getLikeCount()).isZero();
        assertThat(vo.getCommentCount()).isZero();
        assertThat(vo.getViewCount()).isZero();
        assertThat(vo.getStatus()).isEqualTo(1);
        assertThat(vo.getAuthorId()).isEqualTo(USER_A);
    }

    @Test
    @DisplayName("发帖内容正常：无任何焦虑标记，也不返回提醒文案")
    void create_withNormalContent_shouldNotFlag() {
        // given
        Post post = new Post();
        post.setTitle("今天开始练字");
        post.setContent("坚持三天了，孩子自己也很开心");
        when(postMapper.insert(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(POST_ID);
            return 1;
        });
        when(postMapper.selectById(POST_ID)).thenReturn(post);

        // when
        PostVO vo = postService.create(post, USER_A);

        // then
        assertThat(vo.getAnxietyFlag()).isZero();
        assertThat(vo.getWarning()).isNull();
    }

    // ------------------------------------------------------------------
    // 4. 帖子作者归属
    // ------------------------------------------------------------------

    @Test
    @DisplayName("越权编辑他人帖子：拒绝，且不执行更新")
    void updatePost_byNonAuthor_shouldBeRejected() {
        // given：帖子作者是 A，B 试图编辑
        Post exist = post(POST_ID, 1, 1);
        exist.setAuthorId(USER_A);
        when(postMapper.selectById(POST_ID)).thenReturn(exist);

        Post payload = new Post();
        payload.setTitle("被篡改的标题");

        // when & then
        assertThatThrownBy(() -> postService.updatePost(POST_ID, payload, USER_B))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("只能编辑自己发布的帖子");
        verify(postMapper, never()).updateById(any(Post.class));
    }

    @Test
    @DisplayName("越权删除他人帖子：拒绝，且不执行删除")
    void deletePost_byNonAuthor_shouldBeRejected() {
        // given
        Post exist = post(POST_ID, 1, 1);
        exist.setAuthorId(USER_A);
        when(postMapper.selectById(POST_ID)).thenReturn(exist);

        // when & then
        assertThatThrownBy(() -> postService.deletePost(POST_ID, USER_B))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("只能删除自己发布的帖子");
        verify(postMapper, never()).deleteById(any(Serializable.class));
    }

    // ------------------------------------------------------------------
    // 测试辅助
    // ------------------------------------------------------------------

    private static Post post(Long id, Integer likeCount, Integer hotScore) {
        return post(id, likeCount, hotScore, USER_A);
    }

    private static Post post(Long id, Integer likeCount, Integer hotScore, Long authorId) {
        Post p = new Post();
        p.setId(id);
        p.setAuthorId(authorId);
        p.setTitle("标题" + id);
        p.setContent("正文" + id);
        p.setLikeCount(likeCount);
        p.setHotScore(hotScore);
        p.setViewCount(0);
        p.setCommentCount(0);
        p.setStatus(1);
        p.setAnxietyFlag(0);
        return p;
    }

    private static PostLike likeRow(Long postId, Long userId) {
        PostLike row = new PostLike();
        row.setId(1L);
        row.setPostId(postId);
        row.setUserId(userId);
        return row;
    }

    private static UserBrief brief(Long userId) {
        UserBrief brief = new UserBrief();
        brief.setId(userId);
        brief.setNickname("家长" + userId);
        brief.setAvatar("/avatar/" + userId + ".png");
        return brief;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubPage(List<Post> posts) {
        when(postMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(inv -> {
            Page<Post> page = inv.getArgument(0);
            page.setRecords(new ArrayList<>(posts));
            page.setTotal(posts.size());
            return page;
        });
    }

    // ------------------------------------------------------------------
    // 辅助：模拟数据库原子自增 / 自减
    // ------------------------------------------------------------------

    /**
     * 让 {@code postMapper.update(entity, wrapper)} 按 SQL 片段真实改变计数，
     * 从而验证「计数走数据库原子自增」，而不是内存里的「读-改-写」。
     */
    private void bindAtomicCounter(Post post) {
        when(postMapper.update(isNull(), any(Wrapper.class))).thenAnswer(inv -> {
            Wrapper<Post> wrapper = inv.getArgument(1);
            String sqlSet = sqlSetOf(wrapper);
            if (sqlSet == null) {
                return 0;
            }
            int like = post.getLikeCount() == null ? 0 : post.getLikeCount();
            int hot = post.getHotScore() == null ? 0 : post.getHotScore();
            if (sqlSet.contains("like_count + 1")) {
                post.setLikeCount(like + 1);
            } else if (sqlSet.contains("like_count - 1") && like > 0) {
                post.setLikeCount(like - 1);
            }
            if (sqlSet.contains("hot_score + 1")) {
                post.setHotScore(hot + 1);
            } else if (sqlSet.contains("hot_score - 1") && hot > 0) {
                post.setHotScore(hot - 1);
            }
            return 1;
        });
    }

    /**
     * 取出 UpdateWrapper 的 SET 片段，用于断言计数确实交给数据库原子执行。
     */
    @SuppressWarnings("unchecked")
    private static String sqlSetOf(Wrapper<Post> wrapper) {
        return (String) ReflectionTestUtils.invokeMethod(wrapper, "getSqlSet");
    }

    /**
     * 内存版 gx_post_like 表：用 (postId, userId) 唯一约束模拟数据库中的唯一索引，
     * 让「连点」这类高频 toggle 场景能在单测里真实复现。
     */
    private static final class FakeLikeTable {

        private final Map<String, Long> idByKey = new HashMap<>();
        private final Map<Long, String> keyById = new HashMap<>();
        private final AtomicLong sequence = new AtomicLong(1);

        void bindTo(PostLikeMapper mapper) {
            when(mapper.findRawId(anyLong(), anyLong())).thenAnswer(inv ->
                    idByKey.get(key(inv.getArgument(0), inv.getArgument(1))));
            when(mapper.insert(any(PostLike.class))).thenAnswer(inv -> {
                PostLike row = inv.getArgument(0);
                long id = sequence.getAndIncrement();
                String key = key(row.getPostId(), row.getUserId());
                idByKey.put(key, id);
                keyById.put(id, key);
                return 1;
            });
            doAnswer(inv -> {
                Long id = inv.getArgument(0);
                String key = keyById.remove(id);
                if (key != null) {
                    idByKey.remove(key);
                }
                return 1;
            }).when(mapper).deleteById(any(Serializable.class));
        }

        boolean contains(Long postId, Long userId) {
            return idByKey.containsKey(key(postId, userId));
        }

        int size() {
            return idByKey.size();
        }

        private static String key(Long postId, Long userId) {
            return postId + "#" + userId;
        }
    }
}

package com.guanxuege.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.guanxuege.auth.entity.User;
import com.guanxuege.auth.mapper.UserMapper;
import com.guanxuege.community.entity.Mention;
import com.guanxuege.community.mapper.MentionMapper;
import com.guanxuege.community.vo.UserMentionVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @提及服务测试。
 * <p>
 * 守护三条不变量：
 * 1. 容错：@ 不存在的名字只是不产生提及，绝不能让「发帖 / 评论」这条主链路报错；
 * 2. 一致性：save 必须先清后插——否则正文改掉昵称后，旧的提及关系会残留成脏数据；
 * 3. 性能：mapByBizIds 必须批量补齐昵称，不能每条提及查一次用户（N+1）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class MentionServiceImplTest {

    private static final String BIZ_POST = "post";

    @Mock
    private MentionMapper mentionMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MentionServiceImpl mentionService;

    // ------------------------------------------------------------------
    // 1. resolve：昵称 -> 真实用户
    // ------------------------------------------------------------------

    @Test
    @DisplayName("命中真实用户：返回用户 id，昵称取社区昵称（与社区列表展示保持一致）")
    void resolve_hitRealUser_shouldReturnUserWithCommunityNickname() {
        // given：正文 @小明，库中「小明」的社区昵称是「小明妈妈」
        when(userMapper.selectByDisplayNameOrNickname("小明"))
                .thenReturn(List.of(user(7L, "小明", "小明妈妈")));

        // when
        List<UserMentionVO> vos = mentionService.resolve("你好 @小明，请问作业怎么安排");

        // then
        assertThat(vos).hasSize(1);
        assertThat(vos.get(0).getUserId()).isEqualTo(7L);
        assertThat(vos.get(0).getNickname()).isEqualTo("小明妈妈");
    }

    @Test
    @DisplayName("用户没设社区昵称：展示名回落为账号昵称")
    void resolve_withoutCommunityNickname_shouldFallbackToNickname() {
        // given
        when(userMapper.selectByDisplayNameOrNickname("小明"))
                .thenReturn(List.of(user(7L, "小明", null)));

        // when
        List<UserMentionVO> vos = mentionService.resolve("@小明");

        // then
        assertThat(vos).hasSize(1);
        assertThat(vos.get(0).getNickname()).isEqualTo("小明");
    }

    @Test
    @DisplayName("@ 了不存在的名字：忽略且不抛异常，主链路（发帖）不受影响")
    void resolve_unknownName_shouldBeIgnoredSilently() {
        // given：库里查不到「查无此人」
        when(userMapper.selectByDisplayNameOrNickname("查无此人")).thenReturn(List.of());

        // when
        List<UserMentionVO> vos = mentionService.resolve("你好 @查无此人");

        // then
        assertThat(vos).isEmpty();
    }

    @Test
    @DisplayName("查库返回 null（Mapper 边界情况）：同样忽略，不抛空指针")
    void resolve_mapperReturnsNull_shouldBeIgnoredSilently() {
        // given
        when(userMapper.selectByDisplayNameOrNickname("小明")).thenReturn(null);

        // when
        List<UserMentionVO> vos = mentionService.resolve("@小明");

        // then
        assertThat(vos).isEmpty();
    }

    @Test
    @DisplayName("匹配优先级：同名时优先取「社区昵称」命中的用户，而非账号昵称命中的用户")
    void resolve_shouldPreferCommunityNicknameMatch() {
        // given：昵称查询可能一次返回多人（SQL 用 OR 匹配两个字段）
        when(userMapper.selectByDisplayNameOrNickname("明明")).thenReturn(List.of(
                user(1L, "明明", null),      // 账号昵称命中，但社区昵称为空
                user(2L, "另一个人", "明明"))); // 社区昵称命中

        // when
        List<UserMentionVO> vos = mentionService.resolve("@明明");

        // then：命中「社区昵称」的那位优先，这才是用户在社区里看到的那个「明明」
        assertThat(vos).hasSize(1);
        assertThat(vos.get(0).getUserId()).isEqualTo(2L);
        assertThat(vos.get(0).getNickname()).isEqualTo("明明");
    }

    @Test
    @DisplayName("两个不同昵称指向同一个人：按用户 id 去重，只产生一条提及")
    void resolve_sameUserMatchedByTwoNames_shouldDeduplicateByUserId() {
        // given：「小明」和「明明」都是同一个人（账号昵称 / 社区昵称各一）
        User same = user(7L, "小明", "明明");
        when(userMapper.selectByDisplayNameOrNickname("小明")).thenReturn(List.of(same));
        when(userMapper.selectByDisplayNameOrNickname("明明")).thenReturn(List.of(same));

        // when
        List<UserMentionVO> vos = mentionService.resolve("@小明 @明明 你们好");

        // then
        assertThat(vos).hasSize(1);
        assertThat(vos.get(0).getUserId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("正文没有 @：直接返回空列表，一次都不查库")
    void resolve_withoutAt_shouldSkipDatabase() {
        // when
        List<UserMentionVO> vos = mentionService.resolve("今天天气不错");

        // then
        assertThat(vos).isEmpty();
        verify(userMapper, never()).selectByDisplayNameOrNickname(anyString());
    }

    @Test
    @DisplayName("正文塞满 @：最多只解析 20 个昵称，防止刷帖子拖垮发布接口")
    void resolve_moreThan20Mentions_shouldCapAt20() {
        // given：25 个不同的 @，每人都能命中一个独立用户
        AtomicInteger seq = new AtomicInteger(1);
        when(userMapper.selectByDisplayNameOrNickname(anyString())).thenAnswer(inv -> {
            User u = new User();
            u.setId((long) seq.getAndIncrement());
            u.setNickname(inv.getArgument(0));
            return List.of(u);
        });
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            content.append("@user").append(i).append(" ");
        }

        // when
        List<UserMentionVO> vos = mentionService.resolve(content.toString());

        // then：硬上限 20，且查库次数同样被限制在 20 次
        assertThat(vos).hasSize(20);
        verify(userMapper, times(20)).selectByDisplayNameOrNickname(anyString());
    }

    // ------------------------------------------------------------------
    // 2. save：先清后插
    // ------------------------------------------------------------------

    @Test
    @DisplayName("save 必须先删后插：保证提及关系与最新正文一致，不残留旧数据")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void save_shouldRemoveOldRowsBeforeInsert() {
        // given
        when(userMapper.selectByDisplayNameOrNickname("小明"))
                .thenReturn(List.of(user(7L, "小明", null)));
        when(mentionMapper.insert(any(Mention.class))).thenReturn(1);

        // when
        mentionService.save(BIZ_POST, 1001L, "你好 @小明");

        // then：删除一定发生在插入之前
        InOrder order = inOrder(mentionMapper);
        order.verify(mentionMapper).delete(any(Wrapper.class));
        order.verify(mentionMapper).insert(any(Mention.class));
        order.verifyNoMoreInteractions();

        ArgumentCaptor<Mention> captor = ArgumentCaptor.forClass(Mention.class);
        verify(mentionMapper).insert(captor.capture());
        Mention saved = captor.getValue();
        assertThat(saved.getBizType()).isEqualTo(BIZ_POST);
        assertThat(saved.getBizId()).isEqualTo(1001L);
        assertThat(saved.getUserId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("save 命中多人：每人写入一条提及关系，带正确的业务类型与业务 id")
    void save_multipleUsers_shouldInsertOneRowPerUser() {
        // given
        when(userMapper.selectByDisplayNameOrNickname("小明"))
                .thenReturn(List.of(user(7L, "小明", null)));
        when(userMapper.selectByDisplayNameOrNickname("小红"))
                .thenReturn(List.of(user(8L, "小红", null)));
        when(mentionMapper.insert(any(Mention.class))).thenReturn(1);

        // when
        mentionService.save(BIZ_POST, 1001L, "@小明 @小红 来看看");

        // then
        ArgumentCaptor<Mention> captor = ArgumentCaptor.forClass(Mention.class);
        verify(mentionMapper, times(2)).insert(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Mention::getUserId)
                .containsExactly(7L, 8L);
        assertThat(captor.getAllValues())
                .allSatisfy(m -> {
                    assertThat(m.getBizType()).isEqualTo(BIZ_POST);
                    assertThat(m.getBizId()).isEqualTo(1001L);
                });
    }

    @Test
    @DisplayName("正文没有 @：清空历史提及，但绝不写入任何新记录")
    void save_withoutMention_shouldClearButNeverInsert() {
        // when：正文改成了不含 @ 的内容
        mentionService.save(BIZ_POST, 1001L, "改了正文，不再提及任何人");

        // then
        verify(mentionMapper, never()).insert(any(Mention.class));
        verify(userMapper, never()).selectByDisplayNameOrNickname(anyString());
    }

    // ------------------------------------------------------------------
    // 3. mapByBizIds：批量查询，避免 N+1
    // ------------------------------------------------------------------

    @Test
    @DisplayName("bizIds 为空：直接返回空 Map，一次都不查库")
    void mapByBizIds_emptyInput_shouldSkipDatabase() {
        // when
        Map<Long, List<UserMentionVO>> map = mentionService.mapByBizIds(BIZ_POST, List.of());

        // then
        assertThat(map).isEmpty();
        verify(mentionMapper, never()).selectList(any(Wrapper.class));
    }

    @Test
    @DisplayName("bizIds 为 null：同样安全返回空 Map，不抛空指针")
    void mapByBizIds_nullInput_shouldSkipDatabase() {
        // when
        Map<Long, List<UserMentionVO>> map = mentionService.mapByBizIds(BIZ_POST, null);

        // then
        assertThat(map).isEmpty();
        verify(mentionMapper, never()).selectList(any(Wrapper.class));
    }

    @Test
    @DisplayName("批量补齐昵称：3 条提及只涉及 2 个用户时，用户表只查 2 次（不是 3 次）")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void mapByBizIds_shouldQueryEachUserOnlyOnce() {
        // given：帖子 1 @了 101、102，帖子 2 @了 101
        when(mentionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                mention(BIZ_POST, 1L, 101L),
                mention(BIZ_POST, 1L, 102L),
                mention(BIZ_POST, 2L, 101L)));
        when(userMapper.selectById(101L)).thenReturn(user(101L, "小明", "小明妈妈"));
        when(userMapper.selectById(102L)).thenReturn(user(102L, "小红", null));

        // when
        Map<Long, List<UserMentionVO>> map = mentionService.mapByBizIds(BIZ_POST, List.of(1L, 2L));

        // then：按用户 id 去重后才查库
        verify(userMapper, times(1)).selectById(101L);
        verify(userMapper, times(1)).selectById(102L);
        verify(userMapper, times(2)).selectById(anyLong());

        // 结果按业务 id 分桶
        assertThat(map).containsOnlyKeys(1L, 2L);
        assertThat(map.get(1L)).extracting(UserMentionVO::getUserId).containsExactly(101L, 102L);
        assertThat(map.get(2L)).extracting(UserMentionVO::getUserId).containsExactly(101L);
        assertThat(map.get(1L)).extracting(UserMentionVO::getNickname)
                .containsExactly("小明妈妈", "小红");
    }

    @Test
    @DisplayName("被 @ 的用户已注销：该条提及被跳过，且不留下空壳记录")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void mapByBizIds_whenUserDeleted_shouldSkipMention() {
        // given：帖子 1 只 @ 了一个已注销用户
        when(mentionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                mention(BIZ_POST, 1L, 101L)));
        when(userMapper.selectById(101L)).thenReturn(null);

        // when
        Map<Long, List<UserMentionVO>> map = mentionService.mapByBizIds(BIZ_POST, List.of(1L));

        // then：分桶仍保留（前端不需要做空判断），但桶内没有提及
        assertThat(map).containsKey(1L);
        assertThat(map.get(1L)).isEmpty();
    }

    // ------------------------------------------------------------------
    // 4. list：单条内容的提及
    // ------------------------------------------------------------------

    @Test
    @DisplayName("list：只返回仍然存在的用户，已注销用户不再展示提及")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void list_shouldSkipDeletedUser() {
        // given
        when(mentionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                mention(BIZ_POST, 1001L, 101L),
                mention(BIZ_POST, 1001L, 102L)));
        when(userMapper.selectById(101L)).thenReturn(user(101L, "小明", "小明妈妈"));
        when(userMapper.selectById(102L)).thenReturn(null);

        // when
        List<UserMentionVO> vos = mentionService.list(BIZ_POST, 1001L);

        // then
        assertThat(vos).hasSize(1);
        assertThat(vos.get(0).getUserId()).isEqualTo(101L);
        assertThat(vos.get(0).getNickname()).isEqualTo("小明妈妈");
    }

    @Test
    @DisplayName("list：没有提及记录时返回空列表，且不查用户表")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void list_withoutRows_shouldReturnEmpty() {
        // given
        when(mentionMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        // when
        List<UserMentionVO> vos = mentionService.list(BIZ_POST, 1001L);

        // then
        assertThat(vos).isEmpty();
        verify(userMapper, never()).selectById(anyLong());
    }

    // ------------------------------------------------------------------
    // 测试辅助
    // ------------------------------------------------------------------

    private static User user(Long id, String nickname, String communityNickname) {
        User u = new User();
        u.setId(id);
        u.setNickname(nickname);
        u.setCommunityNickname(communityNickname);
        return u;
    }

    private static Mention mention(String bizType, Long bizId, Long userId) {
        Mention m = new Mention();
        m.setBizType(bizType);
        m.setBizId(bizId);
        m.setUserId(userId);
        return m;
    }
}

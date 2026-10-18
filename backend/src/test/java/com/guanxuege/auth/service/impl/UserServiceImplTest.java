package com.guanxuege.auth.service.impl;

import com.guanxuege.auth.dto.UserProfileUpdateDTO;
import com.guanxuege.auth.entity.User;
import com.guanxuege.auth.mapper.ChildMapper;
import com.guanxuege.auth.mapper.UserMapper;
import com.guanxuege.auth.service.ConsentService;
import com.guanxuege.auth.vo.UserProfileVO;
import com.guanxuege.common.exception.BusinessException;
import com.guanxuege.community.mapper.CommentMapper;
import com.guanxuege.community.mapper.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户资料测试——本项目最重要的一条隐私不变量。
 * <p>
 * 手机号 / 微信号 / 邮箱属于联系方式，只在「查看者 == 本人」时才出现在响应里；
 * 他人查看资料页时必须为 null。这条规则一旦被破坏，公开社区就变成了可被批量抓取的
 * 家长联系方式库，属于未成年人相关产品的高危数据泄露。
 * <p>
 * 另一条不变量是「资料接口不可越权改身份」：DTO 只接受昵称类字段，
 * 手机号（登录名）、角色、状态、密码哈希都必须是库里的原值。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserServiceImplTest {

    private static final Long ME = 1L;
    private static final Long OTHER = 2L;

    private static final String PHONE = "13800001111";
    private static final String WECHAT = "guanxuege_2026";
    private static final String EMAIL = "parent@example.com";
    private static final String PASSWORD_HASH = "$2a$10$abcdefghijklmnopqrstuv";
    private static final Integer ROLE = 1;
    private static final Integer STATUS = 1;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ChildMapper childMapper;

    @Mock
    private ConsentService consentService;

    @Mock
    private PostMapper postMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // ------------------------------------------------------------------
    // 1. 隐私：联系方式仅本人可见
    // ------------------------------------------------------------------

    @Test
    @DisplayName("本人查看自己的资料：手机号 / 微信号 / 邮箱全部返回，self 为 true")
    void getProfile_bySelf_shouldExposeContacts() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());
        when(postMapper.selectCount(any())).thenReturn(3L);
        when(commentMapper.selectCount(any())).thenReturn(5L);

        // when
        UserProfileVO vo = userService.getProfile(ME, ME);

        // then
        assertThat(vo.getSelf()).isTrue();
        assertThat(vo.getPhone()).isEqualTo(PHONE);
        assertThat(vo.getWechat()).isEqualTo(WECHAT);
        assertThat(vo.getEmail()).isEqualTo(EMAIL);
        // 公开字段不受影响
        assertThat(vo.getNickname()).isEqualTo("浩浩妈妈");
        assertThat(vo.getSignature()).isEqualTo("陪孩子慢慢长大");
        assertThat(vo.getPostCount()).isEqualTo(3);
        assertThat(vo.getCommentCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("他人查看资料：手机号 / 微信号 / 邮箱必须为 null（自证隐私不变量）")
    void getProfile_byOthers_shouldHideContacts() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());
        when(postMapper.selectCount(any())).thenReturn(3L);
        when(commentMapper.selectCount(any())).thenReturn(5L);

        // when
        UserProfileVO vo = userService.getProfile(ME, OTHER);

        // then：联系方式一个都不能漏出来
        assertThat(vo.getSelf()).isFalse();
        assertThat(vo.getPhone()).isNull();
        assertThat(vo.getWechat()).isNull();
        assertThat(vo.getEmail()).isNull();
        // 但公开信息照常返回，否则资料页就没什么可看的了
        assertThat(vo.getNickname()).isEqualTo("浩浩妈妈");
        assertThat(vo.getCommunityNickname()).isEqualTo("浩妈");
        assertThat(vo.getSignature()).isEqualTo("陪孩子慢慢长大");
        assertThat(vo.getAvatar()).isEqualTo("/avatar/1.png");
        assertThat(vo.getPostCount()).isEqualTo(3);
        assertThat(vo.getCommentCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("未登录游客查看资料（viewerId 为 null）：联系方式同样必须为 null")
    void getProfile_byAnonymous_shouldHideContacts() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        // when
        UserProfileVO vo = userService.getProfile(ME, null);

        // then
        assertThat(vo.getSelf()).isFalse();
        assertThat(vo.getPhone()).isNull();
        assertThat(vo.getWechat()).isNull();
        assertThat(vo.getEmail()).isNull();
        assertThat(vo.getNickname()).isEqualTo("浩浩妈妈");
    }

    // ------------------------------------------------------------------
    // 2. 展示名优先级
    // ------------------------------------------------------------------

    @Test
    @DisplayName("展示名优先级：社区昵称 > 账号昵称")
    void getProfile_withCommunityNickname_shouldPreferItAsDisplayName() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        // when
        UserProfileVO vo = userService.getProfile(ME, ME);

        // then
        assertThat(vo.getCommunityNickname()).isEqualTo("浩妈");
        assertThat(vo.getDisplayName()).isEqualTo("浩妈");
        assertThat(vo.getNickname()).isEqualTo("浩浩妈妈");
    }

    @Test
    @DisplayName("社区昵称为空：展示名回落到账号昵称")
    void getProfile_withoutCommunityNickname_shouldFallbackToNickname() {
        // given
        User user = fullUser();
        user.setCommunityNickname(null);
        when(userMapper.selectById(ME)).thenReturn(user);
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        // when
        UserProfileVO vo = userService.getProfile(ME, ME);

        // then
        assertThat(vo.getDisplayName()).isEqualTo("浩浩妈妈");
    }

    @Test
    @DisplayName("社区昵称为纯空白（脏数据）：视为未设置，同样回落账号昵称")
    void getProfile_withBlankCommunityNickname_shouldFallbackToNickname() {
        // given
        User user = fullUser();
        user.setCommunityNickname("   ");
        when(userMapper.selectById(ME)).thenReturn(user);
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        // when
        UserProfileVO vo = userService.getProfile(ME, ME);

        // then
        assertThat(vo.getDisplayName()).isEqualTo("浩浩妈妈");
    }

    // ------------------------------------------------------------------
    // 3. 目标不存在
    // ------------------------------------------------------------------

    @Test
    @DisplayName("目标用户不存在：抛 BusinessException「用户不存在」")
    void getProfile_userNotExist_shouldThrowBusinessException() {
        // given
        when(userMapper.selectById(999L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> userService.getProfile(999L, ME))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
    }

    @Test
    @DisplayName("目标 id 为 null：抛 BusinessException，不把 null 透传到 Mapper")
    void getProfile_nullTargetId_shouldThrowBusinessException() {
        // when & then
        assertThatThrownBy(() -> userService.getProfile(null, ME))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
        verify(userMapper, never()).selectById(any());
    }

    // ------------------------------------------------------------------
    // 4. updateProfile：字段校验
    // ------------------------------------------------------------------

    @Test
    @DisplayName("更新邮箱：格式合法则写库，并以本人视角返回资料（含联系方式）")
    void updateProfile_validEmail_shouldPersistAndReturnSelfView() {
        // given
        User user = fullUser();
        user.setEmail(null);
        when(userMapper.selectById(ME)).thenReturn(user);
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setEmail("new@example.com");

        // when
        UserProfileVO vo = userService.updateProfile(ME, dto);

        // then
        verify(userMapper).updateById(user);
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        // 返回的资料是本人视角，联系方式可见
        assertThat(vo.getSelf()).isTrue();
        assertThat(vo.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("更新邮箱：格式非法时抛 BusinessException，且绝不写库")
    void updateProfile_invalidEmail_shouldThrowAndNeverUpdate() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setEmail("not-an-email");

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(ME, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("邮箱格式不正确");
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("更新邮箱：缺 @ 或缺少顶级域名的畸形邮箱同样被拒（如 a@b、@example.com）")
    void updateProfile_malformedEmail_shouldThrowAndNeverUpdate() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setEmail("a@b");

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(ME, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("邮箱格式不正确");
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("更新邮箱：超长时有独立文案「邮箱过长」，不再误导成「格式不正确」")
    void updateProfile_tooLongEmail_shouldReportLengthError() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setEmail("a".repeat(60) + "@example.com");

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(ME, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("邮箱过长");
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("更新邮箱：传空字符串表示清除邮箱，而不是写进一个空串")
    void updateProfile_emptyEmail_shouldClearEmail() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setEmail("   ");

        // when
       UserProfileVO vo = userService.updateProfile(ME, dto);

        // then
        assertThat(vo.getEmail()).isNull();
    }

    @Test
    @DisplayName("更新昵称：超过 50 字符抛 BusinessException，且不写库")
    void updateProfile_tooLongNickname_shouldThrowAndNeverUpdate() {
        // given：51 个字符
        when(userMapper.selectById(ME)).thenReturn(fullUser());

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNickname("哈".repeat(51));

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(ME, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("昵称不能超过 50 个字符");
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("更新昵称：恰好 50 字符放行（边界值），且首尾空白被裁剪")
    void updateProfile_nicknameAtLimit_shouldTrimAndPersist() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNickname("  " + "哈".repeat(50) + "  ");

        // when
        UserProfileVO vo = userService.updateProfile(ME, dto);

        // then：先 trim 再校验长度，所以带空白的 50 字仍算合法
        assertThat(vo.getNickname()).hasSize(50);
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("更新社区昵称：超过 30 字符抛 BusinessException，且不写库")
    void updateProfile_tooLongCommunityNickname_shouldThrowAndNeverUpdate() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setCommunityNickname("妈".repeat(31));

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(ME, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("社区昵称不能超过 30 个字符");
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("更新签名：超过 120 字符抛 BusinessException，且不写库")
    void updateProfile_tooLongSignature_shouldThrowAndNeverUpdate() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setSignature("坚".repeat(121));

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(ME, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("签名不能超过 120 个字符");
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("更新微信号：超过 64 字符抛 BusinessException，且不写库")
    void updateProfile_tooLongWechat_shouldThrowAndNeverUpdate() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setWechat("w".repeat(65));

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(ME, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("微信号过长");
        verify(userMapper, never()).updateById(any(User.class));
    }

    // ------------------------------------------------------------------
    // 5. updateProfile：清除语义
    // ------------------------------------------------------------------

    @Test
    @DisplayName("社区昵称传空字符串：被置为 null（表示清除），展示名回落到账号昵称")
    void updateProfile_emptyCommunityNickname_shouldClearAndFallback() {
        // given：原本设置了社区昵称
        when(userMapper.selectById(ME)).thenReturn(fullUser());
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setCommunityNickname("");

        // when
        UserProfileVO vo = userService.updateProfile(ME, dto);

        // then：清除后社区内展示账号昵称
        assertThat(vo.getCommunityNickname()).isNull();
        assertThat(vo.getDisplayName()).isEqualTo("浩浩妈妈");
    }

    @Test
    @DisplayName("签名传空白：被置为 null，而不是存一串空格")
    void updateProfile_blankSignature_shouldClear() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setSignature("   ");

        // when
        UserProfileVO vo = userService.updateProfile(ME, dto);

        // then
        assertThat(vo.getSignature()).isNull();
    }

    @Test
    @DisplayName("微信号传空白：被置为 null，而不是存一串空格")
    void updateProfile_blankWechat_shouldClear() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setWechat("  ");

        // when
        UserProfileVO vo = userService.updateProfile(ME, dto);

        // then
        assertThat(vo.getWechat()).isNull();
    }

    // ------------------------------------------------------------------
    // 6. updateProfile：身份字段不可被篡改
    // ------------------------------------------------------------------

    @Test
    @DisplayName("更新资料不得篡改身份字段：手机号 / 角色 / 状态 / 密码哈希必须是库中原值")
    void updateProfile_shouldNeverTouchIdentityFields() {
        // given
        when(userMapper.selectById(ME)).thenReturn(fullUser());
        when(postMapper.selectCount(any())).thenReturn(0L);
        when(commentMapper.selectCount(any())).thenReturn(0L);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNickname("新昵称");
        dto.setSignature("新签名");

        // when
        userService.updateProfile(ME, dto);

        // then：DTO 里没有 phone / role / status / password 字段，
        // 写库实体必须一字不动地保留库中原值——
        // 防止后续有人改成 BeanUtils.copyProperties(dto, user) 把未传字段抹成 null。
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(ME);
        assertThat(saved.getPhone()).isEqualTo(PHONE);
        assertThat(saved.getRole()).isEqualTo(ROLE);
        assertThat(saved.getStatus()).isEqualTo(STATUS);
        assertThat(saved.getPassword()).isEqualTo(PASSWORD_HASH);
        // 本次真正允许修改的字段
        assertThat(saved.getNickname()).isEqualTo("新昵称");
        assertThat(saved.getSignature()).isEqualTo("新签名");
    }

    @Test
    @DisplayName("更新资料时用户不存在：抛 BusinessException，不写库")
    void updateProfile_userNotExist_shouldThrowAndNeverUpdate() {
        // given
        when(userMapper.selectById(ME)).thenReturn(null);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNickname("新昵称");

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(ME, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
        verify(userMapper, never()).updateById(any(User.class));
    }

    // ------------------------------------------------------------------
    // 测试辅助
    // ------------------------------------------------------------------

    /** 一个资料完整的用户：既有社区昵称，也有联系方式 */
    private static User fullUser() {
        User user = new User();
        user.setId(ME);
        user.setPhone(PHONE);
        user.setPassword(PASSWORD_HASH);
        user.setNickname("浩浩妈妈");
        user.setCommunityNickname("浩妈");
        user.setAvatar("/avatar/1.png");
        user.setWechat(WECHAT);
        user.setEmail(EMAIL);
        user.setSignature("陪孩子慢慢长大");
        user.setRole(ROLE);
        user.setStatus(STATUS);
        return user;
    }
}

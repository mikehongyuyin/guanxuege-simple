package com.guanxuege.common.util;

import com.guanxuege.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 内容安全词库测试（反焦虑护栏）。
 * <p>
 * 两级处理的语义必须被严格区分：
 * - 违禁词：直接拦截（抛 BusinessException），内容不允许发布；
 * - 焦虑词：放行（不抛异常），但返回温和提醒文案并打标；
 * 把焦虑词误做成拦截，会剥夺家长真实的求助表达空间，是产品上的错误。
 */
class WordFilterUtilTest {

    // ------------------------------------------------------------------
    // 违禁词：拦截
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "违禁词「{0}」必须被拦截")
    @ValueSource(strings = {"自杀", "虐待", "人贩子", "吸毒", "赌博", "去死", "傻逼", "他妈的"})
    @DisplayName("命中违禁词：内容被直接拦截")
    void assertSafe_withBannedWord_shouldThrow(String bannedWord) {
        // given
        String text = "今天辅导作业，真的很想" + bannedWord + "，怎么办";

        // when & then
        assertThatThrownBy(() -> WordFilterUtil.assertSafe(text))
                .isInstanceOf(BusinessException.class)
                .hasMessage("内容包含不当词汇，请修改后再发布");
    }

    @Test
    @DisplayName("违禁词出现在正文任意位置（含长文本中间）都能命中")
    void assertSafe_bannedWordInMiddleOfLongText_shouldThrow() {
        // given
        String text = "孩子今年三年级，语文成绩一直上不去，我在家长群里看到有人说遗弃这种念头很危险，想问问大家怎么调节";

        // when & then
        assertThatThrownBy(() -> WordFilterUtil.assertSafe(text))
                .isInstanceOf(BusinessException.class);
    }

    // ------------------------------------------------------------------
    // 焦虑词：放行 + 标记
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "焦虑词「{0}」放行但必须返回提醒文案")
    @ValueSource(strings = {"鸡娃", "牛娃", "神童", "内卷", "躺平", "输在起跑线", "别人家孩子", "恨铁不成钢", "成绩垫底"})
    @DisplayName("命中焦虑词：内容放行，但返回温柔提醒文案（不是拦截）")
    void findAnxietyWarning_withAnxietyWord_shouldReturnWarningInsteadOfBlocking(String anxietyWord) {
        // given
        String text = "最近一直在想怎么" + anxietyWord + "，大家有什么建议吗";

        // when & then：第一步——必须放行，不能抛异常
        assertThatCode(() -> WordFilterUtil.assertSafe(text)).doesNotThrowAnyException();

        // then：第二步——返回命中词对应的温柔文案
        String warning = WordFilterUtil.findAnxietyWarning(text);
        assertThat(warning).isNotNull();
        assertThat(warning).contains(anxietyWord);
        assertThat(warning).contains("每个孩子都有自己的节奏");
    }

    @Test
    @DisplayName("焦虑词命中后返回的文案是完整的安抚话术，而不只是词本身")
    void findAnxietyWarning_shouldReturnFullComfortingMessage() {
        // given
        String text = "别人家孩子都在补课，我很焦虑";

        // when
        String warning = WordFilterUtil.findAnxietyWarning(text);

        // then
        assertThat(warning).isEqualTo(
                "你提到了「别人家孩子」，这个词容易放大焦虑。每个孩子都有自己的节奏，" +
                        "试着从具体的小事开始描述，更容易收获有用的建议。");
    }

    // ------------------------------------------------------------------
    // 正常内容：不拦截也不标记
    // ------------------------------------------------------------------

    @Test
    @DisplayName("正常育儿求助内容：既不拦截，也不打焦虑标记")
    void filter_withNormalContent_shouldPassThrough() {
        // given
        String text = "孩子三年级，数学应用题读不懂，有没有分步练习的方法推荐？";

        // when & then
        assertThatCode(() -> WordFilterUtil.assertSafe(text)).doesNotThrowAnyException();
        assertThat(WordFilterUtil.findAnxietyWarning(text)).isNull();
    }

    @ParameterizedTest(name = "空白内容（{0}）直接放行")
    @NullAndEmptySource
    @DisplayName("内容为空或 null：直接放行，不做任何处理")
    void filter_withBlankContent_shouldPassThrough(String blank) {
        assertThatCode(() -> WordFilterUtil.assertSafe(blank)).doesNotThrowAnyException();
        assertThat(WordFilterUtil.findAnxietyWarning(blank)).isNull();
    }

    // ------------------------------------------------------------------
    // 优先级
    // ------------------------------------------------------------------

    @Test
    @DisplayName("同时命中违禁词与焦虑词：违禁词优先，直接拦截而不是返回提醒")
    void filter_whenBothHit_shouldRejectByBannedWordFirst() {
        // given：既有焦虑词「鸡娃」，也有违禁词「自杀」
        String text = "鸡娃把自己逼得想自杀了";

        // when & then：拦截优先，绝不因为命中焦虑词就放行
        assertThatThrownBy(() -> WordFilterUtil.assertSafe(text))
                .isInstanceOf(BusinessException.class)
                .hasMessage("内容包含不当词汇，请修改后再发布");
    }
}

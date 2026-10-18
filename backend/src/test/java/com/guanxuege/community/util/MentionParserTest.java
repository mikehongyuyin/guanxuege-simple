package com.guanxuege.community.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @提及解析器测试。
 * <p>
 * 这个类是整个 @提及链路的入口，它的不变量是：
 * 1. 昵称按出现顺序返回且去重——正文里 @ 同一个人三次，只应产生一条提及；
 * 2. 遇到空白 / 中英文标点立即截断——否则「@小明，你作业写完没」会解析出「小明，你作业写完没」，
 * 后续查库必然匹配不到用户，功能直接失效；
 * 3. 只负责"取出昵称"，不负责判断用户是否存在——不存在的名字在这里是合法输入，
 * 容错交给 {@link com.guanxuege.community.service.impl.MentionServiceImpl#resolve(String)}。
 */
class MentionParserTest {

    // ------------------------------------------------------------------
    // 1. 基础提取
    // ------------------------------------------------------------------

    @Test
    @DisplayName("单个 @：正确取出昵称，不受前后正文干扰")
    void parse_singleMention_shouldExtractName() {
        // given
        String content = "你好 @小明，想请教下作业怎么安排";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then
        assertThat(names).containsExactly("小明");
    }

    @Test
    @DisplayName("多个 @：按出现顺序返回，顺序错乱会让前端高亮对不上人")
    void parse_multipleMentions_shouldKeepAppearanceOrder() {
        // given
        String content = "@小红 和 @小明 都来看看 @小刚";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then
        assertThat(names).containsExactly("小红", "小明", "小刚");
    }

    @Test
    @DisplayName("同一个名字被 @ 多次：去重，只保留首次出现的位置")
    void parse_repeatedSameName_shouldDeduplicate() {
        // given
        String content = "@小明 @小红 @小明 你们好 @小明";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then
        assertThat(names).containsExactly("小明", "小红");
    }

    @Test
    @DisplayName("中英文昵称混排：均正常提取")
    void parse_mixedLanguageNames_shouldExtractBoth() {
        // given
        String content = "@Tom 说 @小明 也报名了";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then
        assertThat(names).containsExactly("Tom", "小明");
    }

    // ------------------------------------------------------------------
    // 2. 边界截断：@ 后面的内容到哪里为止
    // ------------------------------------------------------------------

    @Test
    @DisplayName("中文标点截断：@小明，/ 。/ ！/ ？/ 、/ ；都只取到「小明」")
    void parse_chinesePunctuation_shouldTruncateName() {
        // given & when & then：中文标点是最常见的书写习惯，必须逐一覆盖
        assertThat(MentionParser.parseNames("@小明，你作业写完没")).containsExactly("小明");
        assertThat(MentionParser.parseNames("@小明。明天见")).containsExactly("小明");
        assertThat(MentionParser.parseNames("@小明！快看")).containsExactly("小明");
        assertThat(MentionParser.parseNames("@小明？")).containsExactly("小明");
        assertThat(MentionParser.parseNames("@小明、小红一起来")).containsExactly("小明");
        assertThat(MentionParser.parseNames("@小明；明天再说")).containsExactly("小明");
        assertThat(MentionParser.parseNames("@小明：你看下")).containsExactly("小明");
    }

    @Test
    @DisplayName("英文标点截断：@Tom, / ! / ? / ; / : / ~ / 引号 / 括号都只取到 Tom")
    void parse_englishPunctuation_shouldTruncateName() {
        // given & when & then
        assertThat(MentionParser.parseNames("@Tom, hello")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("@Tom! hello")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("@Tom? hello")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("@Tom; hello")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("@Tom: hello")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("@Tom~ hello")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("@Tom\" hello")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("@Tom' hello")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("@Tom(hello)")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("@Tom[hello]")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("@Tom{hello}")).containsExactly("Tom");
    }

    @Test
    @DisplayName("空白与换行截断：@Tom hello 只取 Tom，换行同理")
    void parse_whitespace_shouldTruncateName() {
        // given & when & then
        assertThat(MentionParser.parseNames("@Tom hello")).containsExactly("Tom");
        assertThat(MentionParser.parseNames("你好 @小明\n明天见")).containsExactly("小明");
        assertThat(MentionParser.parseNames("@小明\t你好")).containsExactly("小明");
    }

    @Test
    @DisplayName("连续 @@：第一个 @ 因后面紧跟 @ 而失效，第二个 @ 正常命中（不会漏掉真实提及）")
    void parse_doubleAt_shouldFallbackToSecondAt() {
        // given
        String content = "@@小明 你好";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then：@ 被列入排除字符集，因此第一个 @ 不构成提及，但第二个仍要生效
        assertThat(names).containsExactly("小明");
    }

    @Test
    @DisplayName("@ 后面没有名字（紧跟空白或结尾）：不产生任何提及")
    void parse_atWithoutName_shouldReturnEmpty() {
        // given & when & then
        assertThat(MentionParser.parseNames("邮箱是 @ 结尾")).isEmpty();
        assertThat(MentionParser.parseNames("最后再来一个@")).isEmpty();
        assertThat(MentionParser.parseNames("@ ，没有名字")).isEmpty();
    }

    // ------------------------------------------------------------------
    // 3. 空值与退化输入
    // ------------------------------------------------------------------

    @Test
    @DisplayName("null / 空串 / 纯空白：返回空列表，不抛异常")
    void parse_blankOrNull_shouldReturnEmpty() {
        // given & when & then：发布纯图片帖子时正文可能为空，这是真实场景
        assertThat(MentionParser.parseNames(null)).isEmpty();
        assertThat(MentionParser.parseNames("")).isEmpty();
        assertThat(MentionParser.parseNames("   ")).isEmpty();
        assertThat(MentionParser.parseNames("\n\t ")).isEmpty();
    }

    @Test
    @DisplayName("正文中完全没有 @：返回空列表")
    void parse_noAtSign_shouldReturnEmpty() {
        // given
        String content = "今天天气不错，孩子自己把作业写完了";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then
        assertThat(names).isEmpty();
    }

    // ------------------------------------------------------------------
    // 4. 已知边界与缺口（按当前实现断言，行为变更时本测试会失败，提醒同步确认）
    // ------------------------------------------------------------------

    @Test
    @DisplayName("邮箱类文本：英文句号不在截断字符集内，a@b.com 会被解析出「b.com」（已知缺口）")
    void parse_emailLikeText_shouldCaptureDomainPart() {
        // given
        String content = "有问题可以发邮件给我 a@b.com 谢谢";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then
        // 为什么是 b.com：分隔符字符集里只包含空白、@ 与常见中英文标点，
        // 而英文句号「.」不在其中（昵称允许出现点号，如「A.B」），
        // 因此邮箱会被切出域名片段。
        // 影响可控：resolve 阶段要求昵称与库中记录精确相等，
        // 现实中不会有用户叫「b.com」，所以不会产生真实提及，也不会报错。
        assertThat(names).containsExactly("b.com");
    }

    @Test
    @DisplayName("超长名字：正则上限 30 字符，超出部分被丢弃（防止正文塞满 @ 拖慢发布）")
    void parse_overlongName_shouldTruncateAt30() {
        // given：40 个字母，超过 30 字符上限
        String longName = "a".repeat(40);
        String content = "@" + longName + " 你好";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then
        assertThat(names).hasSize(1);
        assertThat(names.get(0)).hasSize(30).isEqualTo("a".repeat(30));
    }

    @Test
    @DisplayName("恰好 30 字符的名字：完整保留，不受上限影响")
    void parse_nameExact30Chars_shouldKeepWhole() {
        // given
        String name30 = "b".repeat(30);
        String content = "@" + name30 + "，你好";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then
        assertThat(names).containsExactly(name30);
    }

    @Test
    @DisplayName("全角括号截断：@小明（你过来一下）只取到「小明」——中文书写里最常见的写法")
    void parse_fullwidthParen_shouldStopAtBracket() {
        // given
        String content = "@小明（你过来一下）";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then
        // 分隔符必须覆盖全角「（）」，否则昵称会带上括号内容，查库匹配不到用户，
        // 表现为「@ 了却没生效」。
        assertThat(names).containsExactly("小明");
    }

    @Test
    @DisplayName("全角书名号与引号截断：【@小红】、「@小刚」只取到昵称本身")
    void parse_otherFullwidthPunctuation_shouldStop() {
        // given
        String content = "【@小红】和「@小刚」都来了";

        // when
        List<String> names = MentionParser.parseNames(content);

        // then
        assertThat(names).containsExactly("小红", "小刚");
    }
}

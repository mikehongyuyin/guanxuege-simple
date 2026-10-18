package com.guanxuege.common.util;

import com.guanxuege.common.exception.BusinessException;

import java.util.List;

/**
 * 内容审核与反焦虑护栏：
 * - 违禁词：直接拦截（发帖/评论不允许发布）
 * - 焦虑词：放行但标记，返回温和提醒文案（不制造新的焦虑）
 */
public final class WordFilterUtil {

    private WordFilterUtil() {
    }

    /** 违禁词：涉及极端/攻击性内容，直接拒绝 */
    private static final List<String> BANNED_WORDS = List.of(
            "自杀", "轻生", "虐待", "遗弃", "弃养", "人贩子", "吸毒", "赌博",
            "打死你", "砍死", "去死", "傻逼", "他妈的", "操你", "滚出");

    /** 焦虑词：常见的「育儿焦虑」表达，命中后标记并温和提醒 */
    private static final List<String> ANXIETY_WORDS = List.of(
            "鸡娃", "牛娃", "神童", "卷死", "内卷", "躺平", "焦虑死",
            "逼疯", "输在起跑线", "别人家孩子", "恨铁不成钢", "成绩垫底", "废了", "完蛋了");

    /** 校验违禁词：命中直接抛出异常 */
    public static void assertSafe(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        for (String w : BANNED_WORDS) {
            if (text.contains(w)) {
                throw new BusinessException("内容包含不当词汇，请修改后再发布");
            }
        }
    }

    /** 检测焦虑词：命中返回温和提醒文案，否则返回 null */
    public static String findAnxietyWarning(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (String w : ANXIETY_WORDS) {
            if (text.contains(w)) {
                return "你提到了「" + w + "」，这个词容易放大焦虑。每个孩子都有自己的节奏，" +
                        "试着从具体的小事开始描述，更容易收获有用的建议。";
            }
        }
        return null;
    }
}

package com.guanxuege.community.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @提及解析：从正文里提取被 @ 的昵称。
 * <p>
 * 匹配规则：{@code @} 后紧跟 1~30 个非空白、非标点字符。
 * 解析只负责"取出昵称"，昵称到用户 id 的匹配交给服务层查库，
 * 这样即便 @ 了一个不存在的名字也不会报错，只是不产生提及关系。
 */
public final class MentionParser {

    /**
     * @昵称：遇到空白、换行或中英文标点即结束。
     * <p>
     * 截断字符集必须同时覆盖半角与**全角**标点——中文书写里「@小明（你过来一下）」很常见，
     * 若只排除半角括号，会连括号一起解析成昵称，导致后续精确匹配失败（表现为「@ 了却没生效」）。
     */
    private static final Pattern MENTION =
            Pattern.compile("@([^\\s@,，。！？、；;:：~!?\"'()\\[\\]{}"
                    + "（）〔〕【】「」『』〈〉《》]{1,30})");

    private MentionParser() {
    }

    /**
     * 解析出正文中被 @ 的昵称（按出现顺序去重）。
     */
    public static List<String> parseNames(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        Matcher matcher = MENTION.matcher(content);
        Set<String> names = new LinkedHashSet<>();
        while (matcher.find()) {
            String name = matcher.group(1).trim();
            if (!name.isBlank()) {
                names.add(name);
            }
        }
        return new ArrayList<>(names);
    }
}

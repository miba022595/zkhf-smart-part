package com.zkhf.epmis.generator.util;

import java.time.LocalDate;
import java.util.Locale;

/**
 * 生成器内部使用的轻量字符串工具类。
 */
public class StringUtils {
    private StringUtils() {
    }

    /**
     * 字符串是否非空。
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }

    /**
     * 首字母转大写。
     */
    public static String capitalize(String value) {
        if (!isNotEmpty(value)) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * 首字母转小写。
     */
    public static String uncapitalize(String value) {
        if (!isNotEmpty(value)) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * 替换字符串中的指定内容。
     */
    public static String replace(String text, String search, String replacement) {
        return text == null ? null : text.replace(search, replacement);
    }

    /**
     * 使用 `{}` 占位符格式化字符串。
     */
    public static String format(String template, Object... args) {
        String result = template;
        for (Object arg : args) {
            result = result.replaceFirst("\\{}", java.util.regex.Matcher.quoteReplacement(String.valueOf(arg)));
        }
        return result;
    }

    /**
     * 安全截取字符串片段。
     */
    public static String substring(String text, int start, int end) {
        if (text == null) {
            return null;
        }
        int safeStart = Math.max(0, start);
        int safeEnd = Math.min(text.length(), end);
        if (safeStart >= safeEnd) {
            return "";
        }
        return text.substring(safeStart, safeEnd);
    }

    /**
     * 下划线命名转小驼峰。
     */
    public static String splitFirstToLowerCamel(String text) {
        if (!isNotEmpty(text)) {
            return text;
        }
        String[] parts = text.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(capitalize(parts[i]));
        }
        return sb.toString();
    }

    /**
     * 下划线命名转大驼峰。
     */
    public static String splitFirstToUpperCamel(String text) {
        if (!isNotEmpty(text)) {
            return text;
        }
        String[] parts = text.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (isNotEmpty(part)) {
                sb.append(capitalize(part));
            }
        }
        return sb.toString();
    }

    /**
     * 按指定分隔符切分字符串。
     */
    public static String[] split(String text, String separator) {
        return text == null ? null : text.split(java.util.regex.Pattern.quote(separator));
    }

    /**
     * 截取两个标记之间的内容。
     */
    public static String substringBetween(String text, String open, String close) {
        if (text == null) {
            return null;
        }
        int start = text.indexOf(open);
        if (start < 0) {
            return null;
        }
        int end = text.indexOf(close, start + open.length());
        if (end < 0) {
            return null;
        }
        return text.substring(start + open.length(), end);
    }

    /**
     * 忽略大小写判断后缀。
     */
    public static boolean endsWithIgnoreCase(String text, String suffix) {
        return text != null && suffix != null && text.toLowerCase(Locale.ROOT).endsWith(suffix.toLowerCase(Locale.ROOT));
    }

    /**
     * 忽略大小写判断是否匹配任一候选值。
     */
    public static boolean equalsAnyIgnoreCase(String value, String... candidates) {
        if (value == null || candidates == null) {
            return false;
        }
        for (String candidate : candidates) {
            if (candidate != null && value.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 安全获取子串首次出现位置。
     */
    public static int indexOf(String text, String search) {
        return text == null ? -1 : text.indexOf(search);
    }

    /**
     * 截取分隔符之前的内容。
     */
    public static String substringBefore(String text, String separator) {
        if (text == null || separator == null) {
            return text;
        }
        int index = text.indexOf(separator);
        return index < 0 ? text : text.substring(0, index);
    }

    /**
     * 获取当前日期字符串。
     */
    public static String today() {
        return LocalDate.now().toString();
    }
}

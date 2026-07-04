package com.xinzhuang.magicspace.utils;

/**
 * 日志安全清洗工具
 * 防止用户输入中的控制字符（换行、回车等）污染日志输出（CRLF 注入）
 */
public class LogSanitizer {

    private LogSanitizer() {
        // 工具类，禁止实例化
    }

    /**
     * 清洗字符串中的控制字符，替换为转义表示
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        return input.replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}

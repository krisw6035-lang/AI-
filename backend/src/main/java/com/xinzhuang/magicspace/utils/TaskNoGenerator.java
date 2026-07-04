package com.xinzhuang.magicspace.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 任务编号生成器
 * 格式：T + yyyyMMddHHmmss + 4位随机数
 */
public final class TaskNoGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private TaskNoGenerator() {}

    public static String generate() {
        String timestamp = LocalDateTime.now().format(FMT);
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "T" + timestamp + random;
    }
}

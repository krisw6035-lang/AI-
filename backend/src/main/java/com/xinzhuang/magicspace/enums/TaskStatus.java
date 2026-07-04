package com.xinzhuang.magicspace.enums;

/**
 * 生成任务状态
 */
public enum TaskStatus {

    WAITING("等待中"),
    RUNNING("生成中"),
    SUCCESS("生成成功"),
    FAILED("生成失败"),
    CANCELED("已取消"),
    TIMEOUT("超时");

    private final String desc;

    TaskStatus(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }
}

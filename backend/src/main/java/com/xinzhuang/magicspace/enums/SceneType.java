package com.xinzhuang.magicspace.enums;

/**
 * 空间改造类型
 */
public enum SceneType {

    INDOOR("室内改装"),
    OUTDOOR("室外改装");

    private final String desc;

    SceneType(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }
}

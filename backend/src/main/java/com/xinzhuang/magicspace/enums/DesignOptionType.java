package com.xinzhuang.magicspace.enums;

/**
 * 设计选项类型
 */
public enum DesignOptionType {

    STYLE("设计风格"),
    TEXTURE("艺术肌理"),
    WALL_COLOR("墙面色号"),
    SOFT_DECORATION("软装细节");

    private final String desc;

    DesignOptionType(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }
}

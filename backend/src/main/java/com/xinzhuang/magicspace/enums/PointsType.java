package com.xinzhuang.magicspace.enums;

/**
 * 积分流水类型
 */
public enum PointsType {

    REGISTER_GIFT("注册赠送"),
    GENERATE_CONSUME("生成消耗"),
    ADMIN_ADD("后台增加"),
    REFUND("失败退回");

    private final String desc;

    PointsType(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }
}

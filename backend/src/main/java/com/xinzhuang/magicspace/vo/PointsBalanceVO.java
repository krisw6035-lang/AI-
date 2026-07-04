package com.xinzhuang.magicspace.vo;

import lombok.Data;

/**
 * 积分余额视图
 */
@Data
public class PointsBalanceVO {

    /** 当前积分余额 */
    private Integer balance;

    /** 每次生成消耗积分 */
    private Integer costPerGenerate;

    /** 剩余可生成次数 */
    private Integer remainingGenerations;

    public static PointsBalanceVO of(int balance, int costPerGenerate) {
        PointsBalanceVO vo = new PointsBalanceVO();
        vo.balance = balance;
        vo.costPerGenerate = costPerGenerate;
        vo.remainingGenerations = costPerGenerate > 0 ? balance / costPerGenerate : 0;
        return vo;
    }
}

package com.xinzhuang.magicspace.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水视图
 */
@Data
public class PointsRecordVO {

    private Long id;
    private Integer changeAmount;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private String recordType;
    private String recordTypeDesc;
    private Long refTaskId;
    private String remark;
    private LocalDateTime createdAt;
}

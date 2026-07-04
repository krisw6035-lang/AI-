package com.xinzhuang.magicspace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水实体
 */
@Data
@TableName("points_record")
public class PointsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 变动积分（正=增加，负=减少） */
    private Integer changeAmount;

    /** 变动前积分 */
    private Integer balanceBefore;

    /** 变动后积分 */
    private Integer balanceAfter;

    /** 流水类型：REGISTER_GIFT/GENERATE_CONSUME/ADMIN_ADD/REFUND */
    private String recordType;

    /** 关联生成任务 ID */
    private Long refTaskId;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

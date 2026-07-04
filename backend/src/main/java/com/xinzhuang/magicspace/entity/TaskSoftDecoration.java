package com.xinzhuang.magicspace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务软装关联实体
 */
@Data
@TableName("task_soft_decoration")
public class TaskSoftDecoration {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 生成任务 ID */
    private Long taskId;

    /** 软装选项 ID */
    private Long optionId;

    /** 软装名称（冗余） */
    private String optionName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

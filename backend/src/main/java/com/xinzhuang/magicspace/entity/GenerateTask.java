package com.xinzhuang.magicspace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生成任务实体
 */
@Data
@TableName("generate_task")
public class GenerateTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务编号（全局唯一） */
    private String taskNo;

    /** 用户 ID */
    private Long userId;

    /** 场景类型：INDOOR/OUTDOOR */
    private String sceneType;

    /** 关联上传图片 ID */
    private Long imageId;

    /** 选中的风格选项 ID */
    private Long styleOptionId;

    /** 风格名称（冗余） */
    private String styleName;

    /** 选中的肌理选项 ID */
    private Long textureOptionId;

    /** 肌理名称（冗余） */
    private String textureName;

    /** 选中的墙面色号选项 ID */
    private Long wallColorId;

    /** 墙面色号（冗余） */
    private String wallColorCode;

    /** 自定义改造要求 */
    private String customRequirement;

    /** 实际发送给 AI 的完整 Prompt */
    private String promptText;

    /** Negative Prompt */
    private String negativePrompt;

    /** 任务状态 */
    private String status;

    /** 生成结果图片 URL */
    private String resultImageUrl;

    /** DashScope 返回的任务 ID */
    private String dashscopeTaskId;

    /** 失败原因 */
    private String errorMessage;

    /** 消耗积分 */
    private Integer pointsCost;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 完成时间 */
    private LocalDateTime completedAt;
}

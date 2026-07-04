package com.xinzhuang.magicspace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 案例实体
 */
@Data
@TableName("case_item")
public class CaseItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 案例标题 */
    private String title;

    /** 案例描述 */
    private String description;

    /** 场景类型：INDOOR/OUTDOOR */
    private String sceneType;

    /** 风格名称 */
    private String styleName;

    /** 改造前图片 URL */
    private String beforeImageUrl;

    /** 改造后图片 URL */
    private String afterImageUrl;

    /** 设计参数（JSON） */
    private String designParams;

    /** 排序 */
    private Integer sortOrder;

    /** 状态：1=展示, 0=隐藏 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

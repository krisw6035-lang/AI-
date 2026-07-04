package com.xinzhuang.magicspace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设计选项实体
 */
@Data
@TableName("design_option")
public class DesignOption {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 选项类型：STYLE/TEXTURE/WALL_COLOR/SOFT_DECORATION */
    private String optionType;

    /** 选项唯一标识 */
    private String optionKey;

    /** 选项名称 */
    private String optionName;

    /** 选项描述 */
    private String optionDesc;

    /** 色值（仅 WALL_COLOR 类型） */
    private String colorHex;

    /** 图标/示例图 URL */
    private String iconUrl;

    /** 适用场景：INDOOR/OUTDOOR/null=通用 */
    private String sceneType;

    /** 排序 */
    private Integer sortOrder;

    /** 状态：1=启用, 0=禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

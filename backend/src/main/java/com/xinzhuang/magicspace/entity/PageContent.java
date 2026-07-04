package com.xinzhuang.magicspace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 页面内容实体
 */
@Data
@TableName("page_content")
public class PageContent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 页面唯一标识：about/contact/explore */
    private String pageKey;

    /** 段落标识 */
    private String sectionKey;

    /** 内容 */
    private String content;

    /** 排序 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

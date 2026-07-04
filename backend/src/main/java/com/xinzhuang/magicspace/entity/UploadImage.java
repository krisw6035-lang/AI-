package com.xinzhuang.magicspace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上传图片实体
 */
@Data
@TableName("upload_image")
public class UploadImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 原始文件名 */
    private String originalName;

    /** 文件存储路径 */
    private String filePath;

    /** 文件访问 URL */
    private String fileUrl;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件类型 */
    private String fileType;

    /** 图片宽度 */
    private Integer width;

    /** 图片高度 */
    private Integer height;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

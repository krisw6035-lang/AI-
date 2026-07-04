package com.xinzhuang.magicspace.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片视图
 */
@Data
public class ImageVO {

    private Long id;
    private String originalName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private Integer width;
    private Integer height;
    private LocalDateTime createdAt;
}

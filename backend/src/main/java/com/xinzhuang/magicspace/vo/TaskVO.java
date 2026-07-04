package com.xinzhuang.magicspace.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生成任务视图
 */
@Data
public class TaskVO {

    private Long id;
    private String taskNo;
    private String sceneType;
    private Long imageId;
    private String originalImageUrl;
    private String styleName;
    private String textureName;
    private String wallColorCode;
    private String wallColorHex;
    private List<String> softDecorationNames;
    private String customRequirement;
    private String status;
    private String resultImageUrl;
    private String errorMessage;
    private Integer pointsCost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}

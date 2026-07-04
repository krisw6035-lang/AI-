package com.xinzhuang.magicspace.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 案例视图
 */
@Data
public class CaseVO {

    private Long id;
    private String title;
    private String description;
    private String sceneType;
    private String styleName;
    private String beforeImageUrl;
    private String afterImageUrl;
    private String designParams;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}

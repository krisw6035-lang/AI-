package com.xinzhuang.magicspace.vo;

import lombok.Data;

/**
 * 设计选项视图
 */
@Data
public class DesignOptionVO {

    private Long id;
    private String optionKey;
    private String optionName;
    private String optionDesc;
    private String colorHex;
    private String iconUrl;
    private String sceneType;
    private Integer sortOrder;
}

package com.xinzhuang.magicspace.ai;

import lombok.Builder;
import lombok.Data;

/**
 * 图像生成请求
 */
@Data
@Builder
public class ImageGenRequest {

    /** 用户上传原图 URL */
    private String imageUrl;

    /** 正向 Prompt */
    private String prompt;

    /** Negative Prompt */
    private String negativePrompt;

    /** 模型名称 */
    private String model;
}

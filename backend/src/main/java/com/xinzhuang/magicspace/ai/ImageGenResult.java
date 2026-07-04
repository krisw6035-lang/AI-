package com.xinzhuang.magicspace.ai;

import lombok.Builder;
import lombok.Data;

/**
 * 图像生成结果
 */
@Data
@Builder
public class ImageGenResult {

    /** 任务状态：RUNNING / SUCCESS / FAILED */
    private String status;

    /** 生成结果图片 URL（成功时返回） */
    private String resultImageUrl;

    /** 失败原因 */
    private String errorMessage;
}

package com.xinzhuang.magicspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户反馈请求
 */
@Data
public class FeedbackDTO {

    /** 反馈类型：BUG/SUGGESTION/OTHER */
    @NotBlank(message = "反馈类型不能为空")
    private String feedbackType;

    /** 反馈内容 */
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 500, message = "反馈内容不能超过500字")
    private String content;

    /** 联系方式（可选） */
    private String contact;
}

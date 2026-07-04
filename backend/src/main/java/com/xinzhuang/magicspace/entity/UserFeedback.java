package com.xinzhuang.magicspace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈实体
 */
@Data
@TableName("user_feedback")
public class UserFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID（可为空） */
    private Long userId;

    /** 反馈类型：BUG/SUGGESTION/OTHER */
    private String feedbackType;

    /** 反馈内容 */
    private String content;

    /** 联系方式 */
    private String contact;

    /** 处理状态：0=未处理, 1=已处理 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

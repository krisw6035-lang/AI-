package com.xinzhuang.magicspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建生成任务请求
 */
@Data
public class CreateTaskDTO {

    /** 场景类型：INDOOR/OUTDOOR */
    @NotBlank(message = "场景类型不能为空")
    private String sceneType;

    /** 上传图片 ID */
    @NotNull(message = "请先上传空间照片")
    private Long imageId;

    /** 设计风格选项 ID */
    @NotNull(message = "请选择设计风格")
    private Long styleOptionId;

    /** 艺术肌理选项 ID（可为空，默认为"无"） */
    private Long textureOptionId;

    /** 墙面色号选项 ID（可为空） */
    private Long wallColorId;

    /** 软装选项 ID 列表（室内场景多选） */
    private List<Long> softDecorationIds;

    /** 自定义改造要求 */
    @Size(max = 200, message = "自定义要求不能超过200字")
    private String customRequirement;
}

package com.xinzhuang.magicspace.ai;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * AI 图生图 Prompt 构建器
 * 根据用户选择的装修设计参数，拼装完整的 prompt 和 negativePrompt
 */
@Component
public class PromptBuilder {

    /**
     * 构建正向 Prompt
     */
    public String buildPrompt(PromptParams params) {
        StringBuilder sb = new StringBuilder();
        sb.append("请基于用户上传的原始空间照片进行 AI 图生图改造，");
        sb.append("必须保持原图的空间结构、透视角度、房间/建筑布局、墙体位置、门窗位置、主要空间关系和主体比例不变，");
        sb.append("不要生成与原图无关的新空间。\n\n");

        sb.append("空间类型：").append("INDOOR".equals(params.sceneType) ? "室内空间" : "室外建筑").append("\n");
        sb.append("设计风格：").append(params.styleName).append("\n");

        if (StringUtils.hasText(params.wallColorCode)) {
            sb.append("墙面颜色参考色号：").append(params.wallColorCode);
            if (StringUtils.hasText(params.wallColorHex)) {
                sb.append("（").append(params.wallColorHex).append("）");
            }
            sb.append("\n");
        }

        if (StringUtils.hasText(params.textureName) && !"无".equals(params.textureName)) {
            sb.append("艺术肌理效果：").append(params.textureName).append("\n");
        }

        if (params.softDecorationNames != null && !params.softDecorationNames.isEmpty()) {
            sb.append("软装细节：").append(String.join("、", params.softDecorationNames)).append("\n");
        }

        if (StringUtils.hasText(params.customRequirement)) {
            String sanitized = sanitizeCustomRequirement(params.customRequirement);
            sb.append("用户额外要求：").append(sanitized).append("\n");
        }

        sb.append("\n请重点优化墙面、材质、家具软装、灯光、空间氛围和整体高级感。");
        sb.append("生成结果必须是");
        sb.append("INDOOR".equals(params.sceneType) ? "室内装修效果图" : "建筑外立面或庭院改造效果图");
        sb.append("，真实自然，适合装修参考。");
        sb.append("不要生成风景图、人像图、商品海报、抽象图或与装修无关的内容。");

        return sb.toString();
    }

    /**
     * 构建 Negative Prompt
     */
    public String buildNegativePrompt() {
        return "不要改变原始空间结构，不要改变透视角度，不要移除主要门窗，"
                + "不要生成完全不同的房间，不要生成与原图无关的建筑，"
                + "不要生成人像、动物、文字海报、纯风景、科幻场景、"
                + "过度夸张效果、低清晰度、畸形家具、扭曲墙体、错误光影。";
    }

    /**
     * 对用户自定义要求做安全清洗，防止 Prompt 注入
     */
    private String sanitizeCustomRequirement(String input) {
        if (input == null) return null;
        return input.replace("\n", " ")
                .replace("\r", " ")
                .replace("---", "")
                .replace("###", "")
                .replace("\"\"\"", "")
                .replace("```", "")
                .trim();
    }

    /**
     * Prompt 参数
     */
    public static class PromptParams {
        public String sceneType;
        public String styleName;
        public String textureName;
        public String wallColorCode;
        public String wallColorHex;
        public List<String> softDecorationNames;
        public String customRequirement;
    }
}

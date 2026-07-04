package com.xinzhuang.magicspace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DashScope 阿里云 AI 服务配置
 */
@Configuration
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeConfig {

    /** API Key — 从环境变量 DASHSCOPE_API_KEY 注入 */
    private String apiKey;

    /** 模型名称，默认 qwen-image-edit-plus（图生图） */
    private String model = "qwen-image-edit-plus";

    /** 连接超时（毫秒） */
    private int connectTimeout = 10000;

    /** 读取超时（毫秒） */
    private int readTimeout = 120000;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }
    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
}

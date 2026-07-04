package com.xinzhuang.magicspace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置
 */
@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {

    /** 存储类型：local / oss */
    private String type = "local";

    /** 本地存储路径 */
    private String localPath = "upload";

    /** 最大上传大小（MB） */
    private int maxSizeMb = 10;

    /** 允许的图片格式 */
    private String allowedFormats = "jpg,jpeg,png,webp";

    // --- OSS 相关（上线时启用）---
    private String ossEndpoint;
    private String ossAccessKeyId;
    private String ossAccessKeySecret;
    private String ossBucketName;

    // getters / setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }
    public int getMaxSizeMb() { return maxSizeMb; }
    public void setMaxSizeMb(int maxSizeMb) { this.maxSizeMb = maxSizeMb; }
    public String getAllowedFormats() { return allowedFormats; }
    public void setAllowedFormats(String allowedFormats) { this.allowedFormats = allowedFormats; }
    public String getOssEndpoint() { return ossEndpoint; }
    public void setOssEndpoint(String ossEndpoint) { this.ossEndpoint = ossEndpoint; }
    public String getOssAccessKeyId() { return ossAccessKeyId; }
    public void setOssAccessKeyId(String ossAccessKeyId) { this.ossAccessKeyId = ossAccessKeyId; }
    public String getOssAccessKeySecret() { return ossAccessKeySecret; }
    public void setOssAccessKeySecret(String ossAccessKeySecret) { this.ossAccessKeySecret = ossAccessKeySecret; }
    public String getOssBucketName() { return ossBucketName; }
    public void setOssBucketName(String ossBucketName) { this.ossBucketName = ossBucketName; }
}

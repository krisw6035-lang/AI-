package com.xinzhuang.magicspace.common;

/**
 * 统一错误码
 */
public enum ErrorCode {

    SUCCESS(0, "成功"),

    // 参数错误
    BAD_REQUEST(40000, "请求参数错误"),
    POINTS_INSUFFICIENT(40001, "积分不足，请获取更多积分后再生成"),
    IMAGE_NOT_FOUND(40002, "图片不存在或无权限"),
    TASK_NOT_FOUND(40003, "生成任务不存在或无权限"),
    DESIGN_OPTION_INVALID(40004, "设计选项无效"),

    // 鉴权错误
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    FORBIDDEN(40300, "无权限"),

    // 限流
    TOO_MANY_REQUESTS(42900, "请求过于频繁，请稍后再试"),

    // 服务端错误
    SYSTEM_ERROR(50000, "系统异常，请稍后重试"),
    AI_SERVICE_ERROR(50001, "AI 服务调用失败，请稍后重试"),
    FILE_UPLOAD_ERROR(50002, "文件上传失败"),
    TASK_TIMEOUT(50003, "任务生成超时，请重新生成");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}

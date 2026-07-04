package com.xinzhuang.magicspace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求
 */
@Data
public class WxLoginDTO {

    @NotBlank(message = "登录凭证不能为空")
    private String code;

    /** 用户昵称（可选，首次登录时传入） */
    private String nickname;

    /** 头像 URL（可选） */
    private String avatarUrl;
}

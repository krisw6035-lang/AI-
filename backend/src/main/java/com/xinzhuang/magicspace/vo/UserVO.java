package com.xinzhuang.magicspace.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息视图
 */
@Data
public class UserVO {

    private Long id;
    private String nickname;
    private String avatarUrl;
    private Integer points;
    private Integer status;
    private LocalDateTime createdAt;
}

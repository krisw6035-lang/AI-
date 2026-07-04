package com.xinzhuang.magicspace.vo;

import lombok.Data;

/**
 * 登录返回视图
 */
@Data
public class LoginVO {

    /** Sa-Token 令牌 */
    private String token;

    /** 用户信息 */
    private UserVO userInfo;

    /** 是否新用户 */
    private Boolean isNewUser;

    public static LoginVO of(String token, UserVO userInfo, boolean isNewUser) {
        LoginVO vo = new LoginVO();
        vo.token = token;
        vo.userInfo = userInfo;
        vo.isNewUser = isNewUser;
        return vo;
    }
}

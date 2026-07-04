package com.xinzhuang.magicspace.controller;

import com.xinzhuang.magicspace.common.BizException;
import com.xinzhuang.magicspace.common.ErrorCode;
import com.xinzhuang.magicspace.common.Result;
import com.xinzhuang.magicspace.dto.WxLoginDTO;
import com.xinzhuang.magicspace.service.UserService;
import com.xinzhuang.magicspace.vo.LoginVO;
import com.xinzhuang.magicspace.vo.UserVO;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final RestTemplate restTemplate;

    @Value("${wx.dev-mode:false}")
    private boolean devMode;

    @Value("${wx.app-id:}")
    private String wxAppId;

    @Value("${wx.app-secret:}")
    private String wxAppSecret;

    public AuthController(UserService userService, RestTemplate restTemplate) {
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    /**
     * 启动时检查：非开发模式下 AppSecret 必须配置
     */
    @PostConstruct
    public void checkConfig() {
        if (!devMode && (wxAppSecret == null || wxAppSecret.isEmpty())) {
            log.error("生产模式下 wx.app-secret 未配置！微信登录将无法工作。请设置 WX_APP_SECRET 环境变量。");
        }
        if (!devMode && (wxAppId == null || wxAppId.isEmpty())) {
            log.error("生产模式下 wx.app-id 未配置！请设置 WX_APP_ID 环境变量。");
        }
    }

    /**
     * 微信登录
     * 开发模式：使用 dev openid 模拟登录
     * 生产模式：调用微信 code2Session 获取真实 openid
     */
    @PostMapping("/wx-login")
    public Result<LoginVO> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        String openid;
        if (devMode) {
            openid = "dev_openid_" + (dto.getCode().hashCode() & 0x7FFFFFFF);
            log.info("开发模式登录: code={}, mappedOpenid={}", dto.getCode(), openid);
        } else {
            openid = resolveOpenid(dto.getCode());
        }

        LoginVO result = userService.wxLogin(openid, dto.getNickname(), dto.getAvatarUrl());
        return Result.ok(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(userService.getCurrentUserVO());
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestParam(required = false) String nickname,
                                        @RequestParam(required = false) String avatarUrl) {
        return Result.ok(userService.updateProfile(nickname, avatarUrl));
    }

    /**
     * 真实微信 code2Session（调用微信官方接口）
     */
    private String resolveOpenid(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + wxAppId
                + "&secret=" + wxAppSecret
                + "&js_code=" + code
                + "&grant_type=authorization_code";

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null) {
                log.error("微信 code2Session 返回空响应");
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }

            // 检查微信返回的错误码
            if (body.containsKey("errcode") && (int) body.get("errcode") != 0) {
                int errcode = (int) body.get("errcode");
                String errmsg = (String) body.getOrDefault("errmsg", "未知错误");
                log.error("微信 code2Session 失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }

            String openid = (String) body.get("openid");
            if (openid == null || openid.isEmpty()) {
                log.error("微信 code2Session 未返回 openid: body={}", body);
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }

            String sessionKey = (String) body.get("session_key");
            log.info("微信 code2Session 成功: openid={}, sessionKey={}",
                    openid, sessionKey != null ? "***" : "null");
            return openid;

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信 code2Session 调用异常: {}", e.getMessage(), e);
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }
    }
}

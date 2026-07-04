package com.xinzhuang.magicspace.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xinzhuang.magicspace.common.Result;
import com.xinzhuang.magicspace.dto.FeedbackDTO;
import com.xinzhuang.magicspace.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户反馈接口
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public Result<?> submit(@Valid @RequestBody FeedbackDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        feedbackService.submit(userId, dto);
        return Result.ok();
    }
}

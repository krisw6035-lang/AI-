package com.xinzhuang.magicspace.controller;

import com.xinzhuang.magicspace.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查接口
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Result<Map<String, Object>> health() {
        return Result.ok(Map.of(
            "status", "UP",
            "service", "房子魔法空间 · 新装",
            "time", LocalDateTime.now().toString()
        ));
    }
}

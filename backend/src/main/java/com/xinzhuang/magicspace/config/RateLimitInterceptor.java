package com.xinzhuang.magicspace.config;

import cn.dev33.satoken.stp.StpUtil;
import com.xinzhuang.magicspace.common.ErrorCode;
import com.xinzhuang.magicspace.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易内存频率限制拦截器
 * 使用滑动窗口算法，按用户/IP 限制请求频率
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final Map<String, long[]> requestLogs = new ConcurrentHashMap<>();

    private static final int LOGIN_LIMIT = 10;
    private static final int GENERATE_LIMIT = 5;
    private static final int UPLOAD_LIMIT = 10;
    private static final long WINDOW_MS = 60_000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equals(method) && path.equals("/api/auth/wx-login")) {
            return checkRate("login:" + getClientIp(request), LOGIN_LIMIT, request, response);
        }
        if ("POST".equals(method) && path.equals("/api/generate/tasks")) {
            String key = "generate:" + resolveUserKey(request);
            return checkRate(key, GENERATE_LIMIT, request, response);
        }
        if ("POST".equals(method) && path.equals("/api/upload/image")) {
            String key = "upload:" + resolveUserKey(request);
            return checkRate(key, UPLOAD_LIMIT, request, response);
        }

        return true;
    }

    private boolean checkRate(String key, int limit, HttpServletRequest request,
                              HttpServletResponse response) throws Exception {
        final int capacity = limit; // effectively final copy
        long now = System.currentTimeMillis();
        long[] timestamps = requestLogs.computeIfAbsent(key, k -> new long[capacity]);

        synchronized (timestamps) {
            int oldestIdx = 0;
            long oldestTime = timestamps[0];
            for (int i = 1; i < timestamps.length; i++) {
                if (timestamps[i] < oldestTime) {
                    oldestTime = timestamps[i];
                    oldestIdx = i;
                }
            }

            if (now - oldestTime < WINDOW_MS) {
                log.warn("频率限制触发: key={}, limit={}, path={}", key, limit, request.getRequestURI());
                response.setStatus(200);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                Result<?> result = Result.fail(ErrorCode.TOO_MANY_REQUESTS);
                response.getWriter().write(objectMapper.writeValueAsString(result));
                return false;
            }

            timestamps[oldestIdx] = now;
        }
        return true;
    }

    /**
     * 优先按用户 ID，未登录时按 IP
     */
    private String resolveUserKey(HttpServletRequest request) {
        try {
            return String.valueOf(StpUtil.getLoginIdAsLong());
        } catch (Exception e) {
            return getClientIp(request);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isEmpty()) {
            return xri;
        }
        return request.getRemoteAddr();
    }
}

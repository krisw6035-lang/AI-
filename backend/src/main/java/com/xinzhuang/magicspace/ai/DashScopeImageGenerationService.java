package com.xinzhuang.magicspace.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinzhuang.magicspace.config.DashScopeConfig;
import com.xinzhuang.magicspace.config.FileStorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 真实 DashScope 图生图服务（阶段 3 实现）
 * <p>
 * 使用 qwen-image-edit-plus 模型，支持参考图 + prompt 进行图像编辑。
 * 采用异步模式：submit() 立即返回，后台线程调用 DashScope API，
 * query() 轮询检查完成状态。
 */
@Service
@ConditionalOnProperty(prefix = "dashscope", name = "api-key")
public class DashScopeImageGenerationService implements ImageGenerationService {

    private static final Logger log = LoggerFactory.getLogger(DashScopeImageGenerationService.class);

    private static final String API_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

    private final DashScopeConfig dashScopeConfig;
    private final FileStorageConfig fileStorageConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /** 异步任务存储 */
    private final Map<String, CompletableFuture<ImageGenResult>> taskStore = new ConcurrentHashMap<>();

    /** 线程池：最多 3 个并发生成任务 */
    private final ExecutorService executor = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "dashscope-gen");
        t.setDaemon(true);
        return t;
    });

    public DashScopeImageGenerationService(DashScopeConfig dashScopeConfig,
                                           FileStorageConfig fileStorageConfig) {
        this.dashScopeConfig = dashScopeConfig;
        this.fileStorageConfig = fileStorageConfig;
        this.objectMapper = new ObjectMapper();

        // 配置 RestTemplate（连接超时 10s，读取超时 120s）
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(dashScopeConfig.getConnectTimeout()));
        factory.setReadTimeout(Duration.ofMillis(dashScopeConfig.getReadTimeout()));
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String submit(ImageGenRequest request) {
        String taskId = UUID.randomUUID().toString().substring(0, 12);

        CompletableFuture<ImageGenResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                return callDashScopeApi(request);
            } catch (Exception e) {
                log.error("DashScope API 调用异常: taskId={}", taskId, e);
                return ImageGenResult.builder()
                        .status("FAILED")
                        .errorMessage("AI 服务调用异常: " + e.getMessage())
                        .build();
            }
        }, executor);

        taskStore.put(taskId, future);

        // 任务完成后 10 分钟清理
        future.thenRun(() -> {
            try {
                Thread.sleep(600_000);
            } catch (InterruptedException ignored) {
            }
            taskStore.remove(taskId);
        });

        log.info("DashScope 图生图任务已提交: taskId={}, imageUrl={}", taskId, request.getImageUrl());
        return taskId;
    }

    @Override
    public ImageGenResult query(String remoteTaskId) {
        CompletableFuture<ImageGenResult> future = taskStore.get(remoteTaskId);
        if (future == null) {
            return ImageGenResult.builder()
                    .status("FAILED")
                    .errorMessage("任务不存在或已过期: " + remoteTaskId)
                    .build();
        }

        if (!future.isDone()) {
            return ImageGenResult.builder()
                    .status("RUNNING")
                    .build();
        }

        try {
            return future.get();
        } catch (Exception e) {
            log.error("获取任务结果异常: taskId={}", remoteTaskId, e);
            return ImageGenResult.builder()
                    .status("FAILED")
                    .errorMessage("获取结果异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 调用 DashScope multimodal-generation API
     */
    private ImageGenResult callDashScopeApi(ImageGenRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("开始调用 DashScope API: model={}", dashScopeConfig.getModel());

        try {
            // 1. 读取上传图片，编码为 base64
            String imageBase64 = encodeImageToBase64(request.getImageUrl());
            if (imageBase64 == null) {
                return ImageGenResult.builder()
                        .status("FAILED")
                        .errorMessage("无法读取上传图片")
                        .build();
            }

            // 2. 构建请求体
            Map<String, Object> requestBody = buildRequestBody(request, imageBase64);

            // 3. 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + dashScopeConfig.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.debug("DashScope 请求已发送，等待响应...");
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, entity, String.class);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("DashScope API 响应: status={}, elapsed={}ms",
                    response.getStatusCode().value(), elapsed);

            // 4. 解析响应
            return parseResponse(response.getBody(), request);

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("DashScope API 调用失败: elapsed={}ms", elapsed, e);
            return ImageGenResult.builder()
                    .status("FAILED")
                    .errorMessage("AI 服务调用失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 构建 DashScope API 请求体
     */
    private Map<String, Object> buildRequestBody(ImageGenRequest request, String imageBase64) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", dashScopeConfig.getModel());

        // input.messages
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");

        List<Map<String, Object>> content = new ArrayList<>();

        // 参考图片（图1）
        Map<String, Object> imageContent = new LinkedHashMap<>();
        imageContent.put("image", imageBase64);
        content.add(imageContent);

        // 文本指令
        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("text", buildEditPrompt(request));
        content.add(textContent);

        message.put("content", content);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("messages", Collections.singletonList(message));
        body.put("input", input);

        // parameters
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("n", 1);
        parameters.put("watermark", false);
        parameters.put("prompt_extend", true);
        if (request.getNegativePrompt() != null && !request.getNegativePrompt().isBlank()) {
            parameters.put("negative_prompt", request.getNegativePrompt());
        }
        body.put("parameters", parameters);

        return body;
    }

    /**
     * 构建图像编辑的文本指令
     * qwen-image-edit 用"图1"指代第一张参考图
     */
    private String buildEditPrompt(ImageGenRequest request) {
        return "图1是一张空间照片。请严格基于图1进行装修改造，必须保持图1的空间结构、"
                + "透视角度、房间/建筑布局、墙体位置、门窗位置不变。\n\n"
                + request.getPrompt();
    }

    /**
     * 解析 DashScope API 响应，下载结果图到本地
     */
    private ImageGenResult parseResponse(String responseBody, ImageGenRequest request) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // 检查是否有错误
            if (root.has("code") && root.has("message")) {
                String code = root.get("code").asText();
                String message = root.get("message").asText();
                log.error("DashScope API 返回错误: code={}, message={}", code, message);
                return ImageGenResult.builder()
                        .status("FAILED")
                        .errorMessage("AI 服务错误 [" + code + "]: " + message)
                        .build();
            }

            // 解析 output.choices[0].message.content
            JsonNode output = root.get("output");
            if (output == null) {
                return ImageGenResult.builder()
                        .status("FAILED")
                        .errorMessage("AI 服务未返回有效结果")
                        .build();
            }

            JsonNode choices = output.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                return ImageGenResult.builder()
                        .status("FAILED")
                        .errorMessage("AI 服务未返回生成结果")
                        .build();
            }

            JsonNode message = choices.get(0).get("message");
            if (message == null) {
                return ImageGenResult.builder()
                        .status("FAILED")
                        .errorMessage("AI 服务返回结果格式异常")
                        .build();
            }

            JsonNode content = message.get("content");
            if (content == null || !content.isArray() || content.size() == 0) {
                return ImageGenResult.builder()
                        .status("FAILED")
                        .errorMessage("AI 服务未返回生成图片")
                        .build();
            }

            // 查找第一个 image 类型的 content
            String dashScopeImageUrl = null;
            for (JsonNode item : content) {
                if (item.has("image")) {
                    dashScopeImageUrl = item.get("image").asText();
                    break;
                }
            }

            if (dashScopeImageUrl == null) {
                return ImageGenResult.builder()
                        .status("FAILED")
                        .errorMessage("AI 服务未返回图片 URL")
                        .build();
            }

            log.info("DashScope 返回结果图: {}", dashScopeImageUrl);

            // 下载结果图到本地（DashScope URL 24 小时后过期）
            String localUrl = downloadResultImage(dashScopeImageUrl);
            log.info("结果图已保存到本地: {}", localUrl);

            return ImageGenResult.builder()
                    .status("SUCCESS")
                    .resultImageUrl(localUrl)
                    .build();

        } catch (Exception e) {
            log.error("解析 DashScope 响应失败", e);
            return ImageGenResult.builder()
                    .status("FAILED")
                    .errorMessage("解析 AI 结果失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 读取本地图片文件并编码为 base64 data URI
     */
    private String encodeImageToBase64(String imageUrl) {
        try {
            // imageUrl 格式如 "/upload/2026/07/03/uuid.jpg"
            // 转换为本地文件路径
            String relativePath = imageUrl.replaceFirst("^/upload/", "");
            Path filePath = Paths.get(fileStorageConfig.getLocalPath(), relativePath);

            if (!Files.exists(filePath)) {
                log.error("图片文件不存在: {}", filePath.toAbsolutePath());
                return null;
            }

            byte[] bytes = Files.readAllBytes(filePath);

            // 根据扩展名确定 MIME 类型
            String mimeType = "image/jpeg";
            String fileName = filePath.getFileName().toString().toLowerCase();
            if (fileName.endsWith(".png")) {
                mimeType = "image/png";
            } else if (fileName.endsWith(".webp")) {
                mimeType = "image/webp";
            } else if (fileName.endsWith(".bmp")) {
                mimeType = "image/bmp";
            }

            String base64 = Base64.getEncoder().encodeToString(bytes);
            String dataUri = "data:" + mimeType + ";base64," + base64;

            log.debug("图片 base64 编码完成: path={}, size={}KB, base64Len={}",
                    relativePath, bytes.length / 1024, base64.length());

            return dataUri;

        } catch (IOException e) {
            log.error("读取图片文件失败: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * 下载 DashScope 返回的结果图到本地存储
     */
    private String downloadResultImage(String dashScopeUrl) {
        try {
            java.net.URI uri = java.net.URI.create(dashScopeUrl);
            URL url = uri.toURL();
            String fileName = url.getPath();
            // 提取文件扩展名
            String ext = "png";
            if (fileName.contains(".")) {
                ext = fileName.substring(fileName.lastIndexOf('.') + 1);
                // 移除可能的查询参数
                if (ext.contains("?")) {
                    ext = ext.substring(0, ext.indexOf('?'));
                }
            }
            // 限制扩展名长度
            if (ext.length() > 5) ext = "png";

            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String storedName = "gen_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
            String relativePath = dateDir + "/" + storedName;

            Path storageDir = Paths.get(fileStorageConfig.getLocalPath(), dateDir);
            Path targetPath = Paths.get(fileStorageConfig.getLocalPath(), relativePath);

            Files.createDirectories(storageDir);

            // 下载图片
            try (InputStream in = url.openStream()) {
                Files.copy(in, targetPath);
            }

            return "/upload/" + relativePath;

        } catch (IOException e) {
            log.error("下载结果图失败: url={}", dashScopeUrl, e);
            // 如果下载失败，返回原始 DashScope URL（24 小时内有效）
            return dashScopeUrl;
        }
    }
}

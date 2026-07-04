package com.xinzhuang.magicspace.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xinzhuang.magicspace.common.Result;
import com.xinzhuang.magicspace.service.ImageService;
import com.xinzhuang.magicspace.vo.ImageVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传接口
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final ImageService imageService;

    public UploadController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * 上传空间图片
     */
    @PostMapping("/image")
    public Result<ImageVO> upload(@RequestParam("file") MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        ImageVO result = imageService.upload(userId, file);
        return Result.ok(result);
    }

    /**
     * 获取图片详情
     */
    @GetMapping("/image/{id}")
    public Result<ImageVO> getImage(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        var image = imageService.getById(id, userId);
        ImageVO vo = new ImageVO();
        vo.setId(image.getId());
        vo.setOriginalName(image.getOriginalName());
        vo.setFileUrl(image.getFileUrl());
        vo.setFileSize(image.getFileSize());
        vo.setFileType(image.getFileType());
        vo.setWidth(image.getWidth());
        vo.setHeight(image.getHeight());
        vo.setCreatedAt(image.getCreatedAt());
        return Result.ok(vo);
    }
}

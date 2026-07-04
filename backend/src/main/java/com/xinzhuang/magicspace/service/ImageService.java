package com.xinzhuang.magicspace.service;

import com.xinzhuang.magicspace.common.BizException;
import com.xinzhuang.magicspace.common.ErrorCode;
import com.xinzhuang.magicspace.config.FileStorageConfig;
import com.xinzhuang.magicspace.entity.UploadImage;
import com.xinzhuang.magicspace.mapper.UploadImageMapper;
import com.xinzhuang.magicspace.vo.ImageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 图片服务
 */
@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final UploadImageMapper uploadImageMapper;
    private final FileStorageConfig fileStorageConfig;
    private final UserService userService;

    public ImageService(UploadImageMapper uploadImageMapper,
                        FileStorageConfig fileStorageConfig,
                        UserService userService) {
        this.uploadImageMapper = uploadImageMapper;
        this.fileStorageConfig = fileStorageConfig;
        this.userService = userService;
    }

    /**
     * 上传图片
     */
    public ImageVO upload(Long userId, MultipartFile file) {
        // 校验格式
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        List<String> allowed = Arrays.asList(fileStorageConfig.getAllowedFormats().split(","));
        if (extension == null || !allowed.contains(extension.toLowerCase())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(),
                    "不支持的图片格式，仅支持: " + fileStorageConfig.getAllowedFormats());
        }

        // 校验文件魔数（防止伪造扩展名）
        if (!isValidImageFile(file)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "文件内容与扩展名不匹配，请上传真实的图片文件");
        }

        // 校验大小
        long maxBytes = (long) fileStorageConfig.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(),
                    "图片大小不能超过 " + fileStorageConfig.getMaxSizeMb() + "MB");
        }

        // 生成存储路径
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storedName = UUID.randomUUID().toString() + "." + extension;
        String relativePath = dateDir + "/" + storedName;

        Path storageDir = Paths.get(fileStorageConfig.getLocalPath(), dateDir);
        Path targetPath = Paths.get(fileStorageConfig.getLocalPath(), relativePath);

        try {
            Files.createDirectories(storageDir);
            // 必须用绝对路径，否则 transferTo 会解析到 Tomcat 临时目录
            file.transferTo(targetPath.toAbsolutePath().toFile());
        } catch (IOException e) {
            log.error("文件写入失败: targetPath={}", targetPath.toAbsolutePath(), e);
            throw new BizException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        // 保存记录
        UploadImage image = new UploadImage();
        image.setUserId(userId);
        image.setOriginalName(originalName);
        image.setFilePath(relativePath);
        image.setFileUrl("/upload/" + relativePath);
        image.setFileSize(file.getSize());
        image.setFileType(extension.toLowerCase());
        uploadImageMapper.insert(image);

        log.info("图片上传成功: id={}, userId={}, path={}", image.getId(), userId, relativePath);

        return toImageVO(image);
    }

    /**
     * 获取图片详情
     */
    public UploadImage getById(Long imageId, Long userId) {
        UploadImage image = uploadImageMapper.selectById(imageId);
        if (image == null) {
            throw new BizException(ErrorCode.IMAGE_NOT_FOUND);
        }
        // 数据隔离：只能查看自己的图片
        if (!image.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.IMAGE_NOT_FOUND);
        }
        return image;
    }

    private ImageVO toImageVO(UploadImage image) {
        ImageVO vo = new ImageVO();
        vo.setId(image.getId());
        vo.setOriginalName(image.getOriginalName());
        vo.setFileUrl(image.getFileUrl());
        vo.setFileSize(image.getFileSize());
        vo.setFileType(image.getFileType());
        vo.setWidth(image.getWidth());
        vo.setHeight(image.getHeight());
        vo.setCreatedAt(image.getCreatedAt());
        return vo;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return null;
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    /**
     * 通过文件头魔数校验是否为真实图片文件
     */
    private boolean isValidImageFile(MultipartFile file) {
        try {
            byte[] header = new byte[12];
            int bytesRead = file.getInputStream().read(header);
            if (bytesRead < 4) return false;

            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return true;
            }

            // PNG: 89 50 4E 47 0D 0A 1A 0A
            if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                return true;
            }

            // WebP: 52 49 46 46 ... 57 45 42 50
            if (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                    && bytesRead >= 12
                    && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
                return true;
            }

            // GIF: 47 49 46 38
            if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) {
                return true;
            }

            // BMP: 42 4D
            if (header[0] == 0x42 && header[1] == 0x4D) {
                return true;
            }

            log.warn("文件魔数校验失败: filename={}, header={:02X} {:02X} {:02X} {:02X}",
                    file.getOriginalFilename(), header[0], header[1], header[2], header[3]);
            return false;
        } catch (IOException e) {
            log.error("文件魔数读取失败: {}", file.getOriginalFilename(), e);
            return false;
        }
    }
}

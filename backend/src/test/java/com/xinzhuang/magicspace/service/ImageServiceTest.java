package com.xinzhuang.magicspace.service;

import com.xinzhuang.magicspace.common.BizException;
import com.xinzhuang.magicspace.config.FileStorageConfig;
import com.xinzhuang.magicspace.mapper.UploadImageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImageServiceTest {

    private ImageService imageService;
    private UploadImageMapper uploadImageMapper;
    private FileStorageConfig fileStorageConfig;
    private UserService userService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        uploadImageMapper = mock(UploadImageMapper.class);
        userService = mock(UserService.class);
        fileStorageConfig = new FileStorageConfig();
        fileStorageConfig.setType("local");
        fileStorageConfig.setLocalPath(tempDir.toString());
        fileStorageConfig.setMaxSizeMb(10);
        fileStorageConfig.setAllowedFormats("jpg,jpeg,png,webp");

        when(uploadImageMapper.insert(any())).thenReturn(1);

        imageService = new ImageService(uploadImageMapper, fileStorageConfig, userService);
    }

    @Test
    void upload_invalidExtension_shouldThrow() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "hello world".getBytes());

        assertThrows(BizException.class, () -> imageService.upload(1L, file));
    }

    @Test
    void upload_invalidMagicBytes_shouldThrow() {
        // File claims to be jpg but content is text
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.jpg", "image/jpeg", "not a real image".getBytes());

        assertThrows(BizException.class, () -> imageService.upload(1L, file));
    }

    @Test
    void upload_validJpegMagic_shouldPassMagicCheck() {
        // Minimal valid JPEG header: FF D8 FF
        byte[] jpegHeader = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01};
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", jpegHeader);

        // Should pass magic byte check (but may fail on other validation)
        // Just verify it doesn't throw magic-byte exception
        try {
            imageService.upload(1L, file);
            // Upload may succeed or fail for other reasons but not magic bytes
        } catch (BizException e) {
            assertFalse(e.getMessage().contains("魔数"), "Should not fail on magic bytes");
        }
    }

    @Test
    void upload_validPngMagic_shouldPassMagicCheck() {
        // PNG header: 89 50 4E 47 0D 0A 1A 0A
        byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D};
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", pngHeader);

        try {
            imageService.upload(1L, file);
        } catch (BizException e) {
            assertFalse(e.getMessage().contains("魔数"), "Should not fail on magic bytes");
        }
    }
}

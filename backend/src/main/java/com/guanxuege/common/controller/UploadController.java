package com.guanxuege.common.controller;

import com.guanxuege.common.R;
import com.guanxuege.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 图片上传：保存到服务器 upload-dir 目录（默认 ./images，容器内为 /app/images），
 * 返回可通过 /api/images/** 访问的 URL，由 Nginx / Vite 反代到后端。
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    /** 上传根目录，可用环境变量 APP_UPLOAD_DIR 覆盖 */
    @Value("${app.upload-dir:./images}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("图片大小不能超过 5MB");
        }
        String ext = extractExt(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg/jpeg/png/gif/webp 格式的图片");
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            file.transferTo(dir.resolve(filename));
            return R.ok("/api/images/" + filename);
        } catch (IOException e) {
            throw new BusinessException("图片保存失败：" + e.getMessage());
        }
    }

    private String extractExt(String name) {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
}

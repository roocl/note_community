package org.notes.service.impl;

import org.notes.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Value("${upload.path}")
    private String uploadBasePath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS
            = Arrays.asList(".jpg", ".jpeg", ".png", ".webp");

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        return doUpload(file);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("图片为空");
        }

        // 校验文件大小
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("图片大小不能超过10MB");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("图片文件名无效");
        }

        // 校验后缀（小写判断）
        String lowerCaseExtension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(lowerCaseExtension)) {
            throw new IllegalArgumentException(
                    "只支持" + ALLOWED_IMAGE_EXTENSIONS + "等格式图片"
            );
        }
        return doUpload(file);
    }

    private String doUpload(MultipartFile file) {
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("文件名无效");
        }

        // 统一生成新文件名
        String fileExtension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase();
        String newFilename = UUID.randomUUID() + fileExtension;

        // 确保目录存在
        File uploadDir = new File(uploadBasePath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IllegalArgumentException("无法创建上传目录" + uploadBasePath);
        }

        // 保存文件
        File destFile = new File(uploadDir, newFilename);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new IllegalStateException("文件保存失败" + e.getMessage(), e);
        }

        // 返回访问URL
        return urlPrefix + "/" + newFilename;
    }
}

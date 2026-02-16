package org.notes.service;

import org.notes.model.vo.upload.ImageVO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    /**
     * 上传图片
     */
    ImageVO uploadImage(MultipartFile file);
}

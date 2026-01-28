package org.notes.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadFile(MultipartFile file);

    String uploadImage(MultipartFile file);
}

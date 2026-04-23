package com.lugares.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadFile(MultipartFile file, String categoria);

    void deleteFile(String relativePath);

    String getPublicUrl(String relativePath);
}

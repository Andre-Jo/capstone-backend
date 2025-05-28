package com.muje.capstone.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;

            Path copyLocation = Paths.get(uploadDir + "/" + fileName);
            Files.copy(file.getInputStream(), copyLocation);

            return fileName; // 파일명만 반환
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다. 다시 시도해 주세요.", e);
        }
    }

    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        try {
            Path filePath = Paths.get(uploadDir + "/" + fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("파일 삭제 실패: " + fileName + ", 오류: " + e.getMessage());
        }
    }
}
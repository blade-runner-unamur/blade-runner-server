package org.unamur.service;

import org.springframework.web.multipart.MultipartFile;

public interface CodeQLService {
    String createDotFile(MultipartFile callCraphFile);

    String extractAlerts(String sarifJson);
}

package org.unamur.service;

import org.springframework.web.multipart.MultipartFile;
import org.unamur.model.PrMetrics;

import java.net.URI;
import java.util.Map;

public interface MetricsService {
    PrMetrics getMetrics(URI projectUrl, String prId);

    void createOrUpdateMetrics(String prId, String projectUrl, Map<String, String> sonarMetrics,
                               MultipartFile sarifFile, MultipartFile impactedFiles,
                               MultipartFile callGraphCsv, String dotFile, String codeqlAlerts, String prSummary);

}

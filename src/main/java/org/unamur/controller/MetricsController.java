package org.unamur.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.unamur.api.MetricsApi;
import org.unamur.model.PrMetrics;
import org.unamur.service.CodeQLService;
import org.unamur.service.MetricsService;
import org.unamur.service.SonarService;

import java.net.URI;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
public class MetricsController implements MetricsApi {

    private final MetricsService metricsService;
    private final SonarService sonarService;
    private final SimpMessagingTemplate template;
    private final CodeQLService codeQlService;

    @Override
    public ResponseEntity<PrMetrics> getMetrics(URI projectUrl, String prId) {
        PrMetrics metrics = metricsService.getMetrics(projectUrl, prId);
        return ResponseEntity.ok(metrics);
    }

    @Override
    public ResponseEntity<Void> postMetrics(String prId, String projectUrl, MultipartFile sarifFile, MultipartFile impactedFiles, MultipartFile callGraphCsv) {
        Map<String, String> sonarMetrics = sonarService.getSonarMetrics();
        String dotFile = codeQlService.createDotFile(callGraphCsv);
        metricsService.createOrUpdateMetrics(prId, projectUrl, sonarMetrics, callGraphCsv, sarifFile, impactedFiles, dotFile);
        template.convertAndSend("/topic/metrics/%s".formatted(prId), "READY");
        return ResponseEntity.ok().build();
    }
}

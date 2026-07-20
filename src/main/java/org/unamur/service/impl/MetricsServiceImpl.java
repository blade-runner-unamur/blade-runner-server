package org.unamur.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.unamur.mapper.PrMetricsMapper;
import org.unamur.model.PrMetrics;
import org.unamur.persistence.PrScanResult;
import org.unamur.repository.PrScanResultRepository;
import org.unamur.service.MetricsService;
import org.unamur.service.SonarService;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@AllArgsConstructor
@Service
public class MetricsServiceImpl implements MetricsService {

    private final PrScanResultRepository prScanResultRepository;
    private final PrMetricsMapper prMetricsMapper;
    private final SonarService sonarService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public PrMetrics getMetrics(URI projectUrl, String prId) {

        var results = prScanResultRepository.findByPrIdAndProjectUrl(prId, projectUrl.toString()).orElseThrow(
                () -> new NoSuchElementException("No scan results found for PR ID: " + prId + " and project URL: " + projectUrl)
        );

        return prMetricsMapper.toDto(results);
    }

    @Override
    public void createOrUpdateMetrics(String prId, String projectUrl, Map<String, String> sonarMetrics, MultipartFile sarifFile, MultipartFile impactedFiles, MultipartFile callGraphCsv, String dotFile, String codeqlAlerts, String prSummary) {
        try {
            String rawSarifJson = new String(sarifFile.getBytes(), StandardCharsets.UTF_8);
            String rawCsv = new String(impactedFiles.getBytes(), StandardCharsets.UTF_8);

            PrScanResult scanResult = prScanResultRepository.findByPrIdAndProjectUrl(prId, projectUrl)
                    .orElse(
                            PrScanResult.builder()
                                    .prId(prId)
                                    .projectUrl(projectUrl)
                                    .rawSarifJson(rawSarifJson)
                                    .impactedFilesCsv(rawCsv)
                                    .build()
                    );


            scanResult.setCoverage(Float.valueOf(sonarMetrics.get("coverage")));
            scanResult.setBugs(Integer.valueOf(sonarMetrics.get("bugs")));
            scanResult.setCodeSmells(Integer.valueOf(sonarMetrics.get("code_smells")));
            scanResult.setVulnerabilities(Integer.valueOf(sonarMetrics.get("vulnerabilities")));
            scanResult.setSecurityHotspots(Integer.valueOf(sonarMetrics.get("security_hotspots")));
            scanResult.setDotFile(dotFile);
            scanResult.setCodeqlAlerts(codeqlAlerts);
            scanResult.setPrSummary(prSummary);
            scanResult.setQualityGateStatus(sonarMetrics.get("alert_status"));
            scanResult.setSonarIssues(fetchSonarIssuesJson(prId));

            if (scanResult.getId() != null) {
                scanResult.setRawSarifJson(rawSarifJson);
                scanResult.setImpactedFilesCsv(rawCsv);
            }

            prScanResultRepository.save(scanResult);

        } catch (IOException e) {
            log.error("Error processing metrics for project {} and PR {}: {}", projectUrl, prId, e.getMessage());
            return; // TODO
        }

        log.info("Processing metrics for project {} and PR {}", projectUrl, prId);
    }

    private String fetchSonarIssuesJson(String prId) {
        try {
            return OBJECT_MAPPER.writeValueAsString(sonarService.getIssues());
        } catch (Exception e) {
            log.warn("Could not fetch/serialize Sonar issues for PR {}: {}", prId, e.getMessage());
            return null;
        }
    }
}

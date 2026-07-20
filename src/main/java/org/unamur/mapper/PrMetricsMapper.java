package org.unamur.mapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.unamur.model.PrMetrics;
import org.unamur.model.SonarData;
import org.unamur.model.SonarIssue;
import org.unamur.persistence.PrScanResult;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PrMetricsMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PrMetrics toDto(PrScanResult entity) {
        if (entity == null) {
            return null;
        }

        PrMetrics dto = new PrMetrics();
        dto.setDotFile(entity.getDotFile());

        SonarData sonarData = new SonarData();
        sonarData.setBugs(entity.getBugs());
        sonarData.setCodeSmells(entity.getCodeSmells());
        sonarData.setVulnerabilities(entity.getVulnerabilities());
        sonarData.setSecurityHotspots(entity.getSecurityHotspots());
        sonarData.setQualityGateStatus(entity.getQualityGateStatus());
        sonarData.setCoverage(entity.getCoverage());
        if (entity.getScanTimestamp() != null) {
            sonarData.setAnalysisDate(entity.getScanTimestamp().atOffset(ZoneOffset.UTC));
        }
        dto.setSonarMetrics(sonarData);
        dto.setCodeqlAlerts(entity.getCodeqlAlerts());
        dto.setSummary(entity.getPrSummary());

        if (entity.getRawSarifJson() != null) {
            try {
                Map<String, Object> sarifMap = objectMapper.readValue(entity.getRawSarifJson(), new TypeReference<>() {
                });
                dto.setSarif(sarifMap);
            } catch (JsonProcessingException e) {
                log.error("Error parsing SARIF JSON for PR ID: {}", entity.getPrId(), e);
            }
        }

        if (entity.getSonarIssues() != null) {
            try {
                List<SonarIssue> issues = objectMapper.readValue(entity.getSonarIssues(), new TypeReference<List<SonarIssue>>() {
                });
                dto.setSonarIssues(issues);
            } catch (JsonProcessingException e) {
                log.error("Error parsing stored Sonar issues for PR ID: {}", entity.getPrId(), e);
            }
        }

        return dto;
    }
}

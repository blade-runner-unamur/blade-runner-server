package org.unamur.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unamur.api.SonarApiClient;
import org.unamur.model.SonarIssue;
import org.unamur.service.SonarService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class SonarServiceImpl implements SonarService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SonarApiClient sonarApiClient;

    public Map<String, String> getSonarMetrics() {
        String responseBody = sonarApiClient.fetchMetrics();
        return parseSonarResponse(responseBody);
    }

    @Override
    public List<SonarIssue> getIssues() {
        List<SonarIssue> issues = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(sonarApiClient.fetchIssues());
            JsonNode arr = root.path("issues");
            if (arr.isArray()) {
                for (JsonNode node : arr) {
                    SonarIssue issue = new SonarIssue();
                    issue.setRule(node.path("rule").asText(null));
                    issue.setSeverity(node.path("severity").asText(null));
                    issue.setType(node.path("type").asText(null));
                    issue.setMessage(node.path("message").asText(null));
                    String component = node.path("component").asText("");
                    int colon = component.indexOf(':');
                    issue.setFile(colon >= 0 ? component.substring(colon + 1) : component);
                    if (node.hasNonNull("line")) {
                        issue.setLine(node.path("line").asInt());
                    }
                    issues.add(issue);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching/parsing Sonar issues", e);
        }
        return issues;
    }

    // Helper method for parsing the JSON
    private Map<String, String> parseSonarResponse(String jsonResponse) {
        Map<String, String> parsedMetrics = new HashMap<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode measures = root.path("component").path("measures");

            if (measures.isArray()) {
                for (JsonNode measure : measures) {
                    parsedMetrics.put(measure.get("metric").asText(), measure.get("value").asText());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing JSON from SonarCloud", e);
        }
        return parsedMetrics;
    }
}

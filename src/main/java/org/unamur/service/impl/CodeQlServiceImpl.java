package org.unamur.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.unamur.service.CodeQLService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Slf4j
@Service
@AllArgsConstructor
public class CodeQlServiceImpl implements CodeQLService {

    @Override
    public String createDotFile(MultipartFile callGraphFile) {

        StringBuilder dotBuilder = new StringBuilder();
        dotBuilder.append("digraph PR_Impact_Analysis {\n");
        dotBuilder.append("  rankdir=LR;\n");
        dotBuilder.append("  node [shape=rectangle, style=filled, fontname=\"Helvetica\"];\n");

        // Define colors
        String prColor = "#FFB3BA";      // Light Red for PR files
        String externalColor = "#BAFFC9"; // Light Green for interacting classes


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(callGraphFile.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                log.info("Line: {}", line);

                String[] columns = line.split(",");

                if (columns.length >= 2) {
                    String caller = columns[0].trim();
                    String callee = columns[1].trim();

                    if (!caller.isEmpty() && !callee.isEmpty()) {
                        dotBuilder.append("    \"%s\" -> \"%s\";\n".formatted(caller, callee));
                    }
                }
            }

            dotBuilder.append("}\n");

            return dotBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the uploaded CSV file.", e);
        }
    }

    @Override
    public String extractAlerts(String sarifJson) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(sarifJson);
            ArrayNode alertsArray = mapper.createArrayNode();

            JsonNode runs = root.get("runs");
            if (runs != null && runs.isArray()) {
                for (JsonNode run : runs) {
                    JsonNode results = run.get("results");
                    if (results != null && results.isArray()) {
                        for (JsonNode result : results) {
                            ObjectNode alert = mapper.createObjectNode();
                            alert.put("ruleId", result.path("ruleId").asText());
                            alert.put("message", result.path("message").path("text").asText());
                            alert.set("location", result.path("locations").path(0).path("physicalLocation"));
                            alertsArray.add(alert);
                        }
                    }
                }
            }
            return mapper.writeValueAsString(alertsArray);
        } catch (IOException e) {
            log.error("Error extracting alerts from SARIF JSON", e);
            return "[]";
        }
    }
}

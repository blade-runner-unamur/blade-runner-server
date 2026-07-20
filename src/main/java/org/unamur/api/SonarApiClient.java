package org.unamur.api;

import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.unamur.config.AppProperties;
import org.unamur.config.TokenProperties;

@Component
public class SonarApiClient {

    private final RestClient client;
    private final AppProperties appProperties;

    public SonarApiClient(TokenProperties tokenProperties, AppProperties appProperties) {
        this.appProperties = appProperties;
        this.client = RestClient.builder()
                .baseUrl("https://sonarcloud.io/api")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProperties.getSonarToken())
                .build();
    }

    public String fetchMetrics() {
        String metricsToFetch = "alert_status,bugs,vulnerabilities,code_smells,coverage,security_hotspots";

        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/measures/component")
                        .queryParam("component", appProperties.getSonarComponentKey())
                        .queryParam("metricKeys", metricsToFetch)
                        .build())
                .retrieve()
                .body(String.class);

    }

    public String fetchIssues() {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/issues/search")
                        .queryParam("componentKeys", appProperties.getSonarComponentKey())
                        .queryParam("resolved", "false")
                        .queryParam("ps", "100")
                        .queryParam("s", "SEVERITY")
                        .queryParam("asc", "false")
                        .build())
                .retrieve()
                .body(String.class);
    }
}

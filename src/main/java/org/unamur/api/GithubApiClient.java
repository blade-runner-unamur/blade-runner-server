package org.unamur.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.unamur.config.AppProperties;

import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Component
public class GithubApiClient {

    private final WebClient githubWebClient;

    private final AppProperties appProperties;

    private final static String WORKFLOW_ID = "scanner.yaml";

    public List<Map<String, Object>> getOpenPrForProject(String owner, String repository) {
        return githubWebClient.get()
                .uri("/repos/{owner}/{repo}/pulls?state=open", owner, repository)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
    }

    public void triggerScannerForPullRequest(Map<String, Object> variables) {
        log.info("Triggering /repos/%s/%s/actions/workflows/%s/dispatches".formatted(appProperties.getGithubOwner(), appProperties.getGithubWorkerRepo(), WORKFLOW_ID));

        log.info("Variables: {}", variables);

        githubWebClient.post()
                .uri("/repos/{githubOwner}/{githubWorkerRepo}/actions/workflows/{workflowId}/dispatches", appProperties.getGithubOwner(), appProperties.getGithubWorkerRepo(), WORKFLOW_ID)
                .header(org.springframework.http.HttpHeaders.ACCEPT, "application/vnd.github+json")
                .bodyValue(variables)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

}

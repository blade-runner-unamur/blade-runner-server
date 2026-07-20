package org.unamur.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public List<Map<String, Object>> getOpenPrForProject(String owner, String repository) {
        String body = githubWebClient.get()
                .uri("/repos/{owner}/{repo}/pulls?state=open", owner, repository)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).defaultIfEmpty("")
                                .map(errorBody -> new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                                        "GitHub API returned %s for %s/%s: %s"
                                                .formatted(response.statusCode(), owner, repository, errorBody))))
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = OBJECT_MAPPER.readTree(body == null ? "" : body);
            if (!root.isArray()) {
                String message = root.path("message").asText(body);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "GitHub did not return a PR list for %s/%s: %s".formatted(owner, repository, message));
            }
            return OBJECT_MAPPER.convertValue(root, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Invalid JSON from GitHub for %s/%s".formatted(owner, repository), e);
        }
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

    public String getPullRequestDiff(String owner, String repository, int pullNumber) {
        log.info("Fetching diff for PR #{} in {}/{}", pullNumber, owner, repository);

        return githubWebClient.get()
                .uri("/repos/{owner}/{repo}/pulls/{pullNumber}", owner, repository, pullNumber)
                .header(org.springframework.http.HttpHeaders.ACCEPT, "application/vnd.github.v3.diff")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

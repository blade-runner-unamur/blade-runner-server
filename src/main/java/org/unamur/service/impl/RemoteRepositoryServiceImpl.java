package org.unamur.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unamur.api.GithubApiClient;
import org.unamur.config.AppProperties;
import org.unamur.persistence.PullRequestMetadata;
import org.unamur.repository.FileDifMetadataRepository;
import org.unamur.repository.PullRequestMetadataRepository;
import org.unamur.service.RemoteRepositoryService;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class RemoteRepositoryServiceImpl implements RemoteRepositoryService {

    private final AppProperties appProperties;

    private final GithubApiClient githubApiClient;

    private final DiffVisualizerService diffVisualizerService;

    private PullRequestMetadataRepository pullRequestMetadataRepository;
    private FileDifMetadataRepository fileDifMetadataRepository;

    @Override
    public List<Map<String, Object>> listPR(String repositoryUrl) {
        // Extract owner and repo from URL
        // Example: https://github.com/pjalbrz16/my-app.git -> ["pjalbrz16", "my-app"]
        String path = repositoryUrl.replace("https://github.com/", "").replace(".git", "");
        String[] parts = path.split("/");
        String owner = parts[0];
        String repo = parts[1];

        return githubApiClient.getOpenPrForProject(owner, repo);
    }

    @Override
    public void triggerScannerForPullRequest(URI projectUrl, String prId) {

        Map<String, Object> variables = Map.of(
                "ref", "main", // The branch where the workflow file exists
                "inputs", Map.of(
                        "prId", prId,
                        "projectUrl", projectUrl,
                        "backendUrl", appProperties.getUrl()
                )
        );

        githubApiClient.triggerScannerForPullRequest(variables);
        log.info("Triggered scanner for project {} with PR {}", projectUrl, prId);
    }

    @Override
    public String getPullRequestDiff(String owner, String repository, String pullNumber) {
        int prNum = Integer.parseInt(pullNumber);
        Optional<PullRequestMetadata> existing = pullRequestMetadataRepository.findByOwnerAndRepositoryAndPrNumber(owner, repository, prNum);

        String diff = this.githubApiClient.getPullRequestDiff(owner, repository, prNum);
        String generatedSvgFromDiff = diffVisualizerService.generateSvgFromDiff(diff);

        PullRequestMetadata pullRequestMetadata;
        if (existing.isPresent()) {
            pullRequestMetadata = existing.get();
            // Clear existing files and chunks before rebuilding
            PullRequestMetadata newMetadata = diffVisualizerService.buildEntitiesFromDiff(owner, repository, prNum, diff);
            
            pullRequestMetadata.setRawDiff(diff);
            pullRequestMetadata.setCachedSvg(generatedSvgFromDiff);
            
            // Re-sync files
            pullRequestMetadata.getFiles().clear();
            for (org.unamur.persistence.FileDiffMetadata file : newMetadata.getFiles()) {
                pullRequestMetadata.addFile(file);
            }
        } else {
            pullRequestMetadata = diffVisualizerService.buildEntitiesFromDiff(owner, repository, prNum, diff);
            pullRequestMetadata.setRawDiff(diff);
            pullRequestMetadata.setCachedSvg(generatedSvgFromDiff);
        }

        pullRequestMetadataRepository.save(pullRequestMetadata);
        return pullRequestMetadata.getCachedSvg();
    }
    
}

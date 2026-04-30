package org.unamur.service;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface RemoteRepositoryService {
    List<Map<String, Object>> listPR(String repositoryUrl);

    void triggerScannerForPullRequest(URI projectUrl, String prId);

    String getPullRequestDiff(String owner, String repository, String pullNumber);
}

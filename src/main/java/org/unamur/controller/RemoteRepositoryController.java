package org.unamur.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.unamur.api.RemoteRepositoryApi;
import org.unamur.model.PullRequestInfo;
import org.unamur.service.RemoteRepositoryService;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@RestController
public class RemoteRepositoryController implements RemoteRepositoryApi {

    private final RemoteRepositoryService remoteRepositoryService;

    @Override
    public ResponseEntity<List<PullRequestInfo>> listPrGet(URI projectUrl) {
        var data = remoteRepositoryService.listPR(projectUrl.toString());

        List<PullRequestInfo> dtos = data.stream().map(pr -> {
            var pullRequestInfo = new PullRequestInfo();
            pullRequestInfo.setId(pr.get("number").toString());
            pullRequestInfo.setTitle((String) pr.get("title"));
            pullRequestInfo.setAuthor((String) ((Map) pr.get("user")).get("login"));
            pullRequestInfo.setUrl((String) pr.get("html_url"));
            return pullRequestInfo;
        }).toList();

        return ResponseEntity.of(Optional.of(dtos));
    }

    @Override
    public ResponseEntity<Void> addProjectPullRequestPost(URI projectUrl, String prId) {
        remoteRepositoryService.triggerScannerForPullRequest(projectUrl, prId);
        return ResponseEntity.ok().build();
    }
}

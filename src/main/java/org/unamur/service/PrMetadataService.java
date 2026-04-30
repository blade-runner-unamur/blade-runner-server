package org.unamur.service;

import org.unamur.model.ChunkDiffMetadata;
import org.unamur.persistence.FileDiffMetadata;
import org.unamur.persistence.PullRequestMetadata;

public interface PrMetadataService {

    ChunkDiffMetadata retreiveChunkDiffMetadata(String owner, String repository, Integer pullRequestNumber, String filename, Integer chunkIndex);

    FileDiffMetadata retreiveFileDiffMetadata(String owner, String repository, Integer pullRequestNumber, String filename);

    PullRequestMetadata retreivePullRequestMetadata(String owner, String repository, Integer pullRequestNumber);
}

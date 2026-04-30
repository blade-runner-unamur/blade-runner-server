package org.unamur.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.unamur.model.ChunkDiffMetadata;
import org.unamur.persistence.FileDiffMetadata;
import org.unamur.persistence.PullRequestMetadata;
import org.unamur.repository.FileDifMetadataRepository;
import org.unamur.repository.PullRequestMetadataRepository;
import org.unamur.service.PrMetadataService;

@Service
@AllArgsConstructor
public class PrMetadataServiceImpl implements PrMetadataService {

    private final PullRequestMetadataRepository prRepository;
    private final FileDifMetadataRepository fileRepository;

    @Override
    public ChunkDiffMetadata retreiveChunkDiffMetadata(String owner, String repository, Integer pullRequestNumber, String filename, Integer chunkIndex) {
        return prRepository.findByOwnerAndRepositoryAndPrNumber(owner, repository, pullRequestNumber)
                .flatMap(pr -> fileRepository.findByPullRequestIdAndFilename(pr.getId(), filename))
                .flatMap(file -> file.getChunks().stream()
                        .filter(chunk -> chunk.getChunkIndex().equals(chunkIndex))
                        .findFirst()
                )
                .map(chunk -> {
                    ChunkDiffMetadata dto = new ChunkDiffMetadata();
                    dto.setId(chunk.getId());
                    dto.setChunkIndex(chunk.getChunkIndex());
                    dto.setAddedLinesCount(chunk.getAddedLinesCount());
                    dto.setDeletedLinesCount(chunk.getDeletedLinesCount());
                    dto.setRawCodeLines(chunk.getRawCodeLines());
                    return dto;
                })
                .orElse(null);
    }

    @Override
    public FileDiffMetadata retreiveFileDiffMetadata(String owner, String repository, Integer pullRequestNumber, String filename) {
        return prRepository.findByOwnerAndRepositoryAndPrNumber(owner, repository, pullRequestNumber)
                .flatMap(pr -> fileRepository.findByPullRequestIdAndFilename(pr.getId(), filename))
                .orElse(null);
    }

    @Override
    public PullRequestMetadata retreivePullRequestMetadata(String owner, String repository, Integer pullRequestNumber) {
        return prRepository.findByOwnerAndRepositoryAndPrNumber(owner, repository, pullRequestNumber)
                .orElse(null);
    }
}

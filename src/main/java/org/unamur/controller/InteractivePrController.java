package org.unamur.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.unamur.api.InteractivePrApi;
import org.unamur.mapper.PrMetadataMapper;
import org.unamur.model.ChunkDiffMetadata;
import org.unamur.model.FileDiffMetadata;
import org.unamur.service.PrMetadataService;

@Slf4j
@AllArgsConstructor
@RestController
public class InteractivePrController implements InteractivePrApi {

    private final PrMetadataService prMetadataService;
    private final PrMetadataMapper prMetadataMapper;

    @Override
    public ResponseEntity<ChunkDiffMetadata> getChunkDiffMetadata(String owner, String repository, Integer pullRequestNumber, String filename, Integer chunkIndex) {
        ChunkDiffMetadata result = prMetadataService.retreiveChunkDiffMetadata(owner, repository, pullRequestNumber, filename, chunkIndex);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<FileDiffMetadata> getFileDiffMetadata(String owner, String repository, Integer pullRequestNumber, String filename) {
        org.unamur.persistence.FileDiffMetadata entity = prMetadataService.retreiveFileDiffMetadata(owner, repository, pullRequestNumber, filename);
        return entity != null ? ResponseEntity.ok(prMetadataMapper.toFileDto(entity)) : ResponseEntity.notFound().build();
    }
}

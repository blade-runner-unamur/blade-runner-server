package org.unamur.mapper;

import org.springframework.stereotype.Component;
import org.unamur.model.ChunkDiffMetadata;
import org.unamur.model.FileDiffMetadata;
import org.unamur.persistence.LineDiffMetadata;

import java.util.stream.Collectors;

@Component
public class PrMetadataMapper {

    public FileDiffMetadata toFileDto(org.unamur.persistence.FileDiffMetadata entity) {
        if (entity == null) {
            return null;
        }

        FileDiffMetadata dto = new FileDiffMetadata();
        dto.setId(entity.getId());
        dto.setFilename(entity.getFilename());
        dto.setStatus(FileDiffMetadata.StatusEnum.fromValue(entity.getStatus().name()));
        
        if (entity.getChunks() != null) {
            dto.setChunks(entity.getChunks().stream()
                    .map(this::toChunkDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public ChunkDiffMetadata toChunkDto(LineDiffMetadata entity) {
        if (entity == null) {
            return null;
        }

        ChunkDiffMetadata dto = new ChunkDiffMetadata();
        dto.setId(entity.getId());
        dto.setChunkIndex(entity.getChunkIndex());
        dto.setAddedLinesCount(entity.getAddedLinesCount());
        dto.setDeletedLinesCount(entity.getDeletedLinesCount());
        dto.setRawCodeLines(entity.getRawCodeLines());

        return dto;
    }
}

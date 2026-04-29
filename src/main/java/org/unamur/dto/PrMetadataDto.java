package org.unamur.dto;

import java.util.List;

public record PrMetadataDto(
        String mergeBase,
        String prReference,
        List<String> impactedFiles,
        String workingDir
) {}

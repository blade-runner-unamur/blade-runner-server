package org.unamur.dto;

public record ImpactedClassDto(
        String className,
        int alertCount,
        boolean inNewFile
) {}

package org.unamur.dto;

import java.util.List;

public record PrMetricsDto(
   int criticalAlerts,
   int totalImpactedFiles,
   double riskScore,
   List<ImpactedClassDto> impactedClassDtos
) {}

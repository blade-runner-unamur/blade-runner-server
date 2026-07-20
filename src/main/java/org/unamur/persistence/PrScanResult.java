package org.unamur.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pr_scan_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String prId;

    @Column(nullable = false)
    private String projectUrl;

    private String qualityGateStatus;

    private Integer bugs;

    private Integer vulnerabilities;

    private Integer codeSmells;

    private Float coverage;

    private Integer securityHotspots;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawSarifJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String impactedFilesCsv;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String dotFile;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String codeqlAlerts;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String prSummary;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String sonarIssues;

    @CreationTimestamp
    @Column(name = "scan_timestamp", updatable = false)
    private LocalDateTime scanTimestamp;
}

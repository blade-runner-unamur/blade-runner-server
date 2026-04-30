package org.unamur.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table
public class LineDiffMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "added_lines_count")
    private Integer addedLinesCount;

    @Column(name = "deleted_lines_count")
    private Integer deletedLinesCount;

    @Column(name = "raw_code_lines", columnDefinition = "TEXT")
    private String rawCodeLines;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_diff_id", nullable = false)
    private FileDiffMetadata fileDiff;
}
package org.unamur.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.unamur.enums.FileStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pr_file_diffs")
@Getter
@Setter
public class FileDiffMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequestMetadata pullRequest;

    @OneToMany(mappedBy = "fileDiff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineDiffMetadata> chunks = new ArrayList<>();

    public void addChunk(LineDiffMetadata chunk) {
        chunks.add(chunk);
        chunk.setFileDiff(this);
    }
}

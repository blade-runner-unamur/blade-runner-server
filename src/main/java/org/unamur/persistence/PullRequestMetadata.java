package org.unamur.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pull_requests", uniqueConstraints = {
        // Ensures we don't save the same PR twice for the same repo
        @UniqueConstraint(columnNames = {"owner", "repository", "pr_number"})
})
@Getter
@Setter
public class PullRequestMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String repository;

    @Column(name = "pr_number", nullable = false)
    private Integer prNumber;

    @Column(name = "cached_svg", columnDefinition = "TEXT")
    private String cachedSvg;

    @Column(name = "raw_diff", columnDefinition = "TEXT")
    private String rawDiff;

    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileDiffMetadata> files = new ArrayList<>();

    public void addFile(FileDiffMetadata file) {
        files.add(file);
        file.setPullRequest(this);
    }
}

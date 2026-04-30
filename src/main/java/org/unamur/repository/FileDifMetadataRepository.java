package org.unamur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unamur.persistence.FileDiffMetadata;

import java.util.Optional;

@Repository
public interface FileDifMetadataRepository extends JpaRepository<FileDiffMetadata, Long> {
    Optional<FileDiffMetadata> findByPullRequestIdAndFilename(Long pullRequestId, String filename);
}

package org.unamur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unamur.persistence.PullRequestMetadata;

import java.util.Optional;

@Repository
public interface PullRequestMetadataRepository extends JpaRepository<PullRequestMetadata, Long> {
    Optional<PullRequestMetadata> findByOwnerAndRepositoryAndPrNumber(String owner, String repository, Integer prNumber);
}

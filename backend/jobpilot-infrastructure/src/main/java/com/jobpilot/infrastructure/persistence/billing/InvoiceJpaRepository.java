package com.jobpilot.infrastructure.persistence.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, UUID> {
    List<InvoiceEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

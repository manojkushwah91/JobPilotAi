package com.jobpilot.infrastructure.persistence.billing;

import com.jobpilot.application.billing.ports.InvoiceRepository;
import com.jobpilot.domain.billing.Invoice;
import com.jobpilot.domain.billing.InvoiceId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final InvoiceJpaRepository jpaRepository;

    public InvoiceRepositoryImpl(InvoiceJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }

    @Override
    public Invoice save(Invoice invoice) {
        return jpaRepository.save(InvoiceEntity.fromDomain(invoice)).toDomain();
    }

    @Override
    public Optional<Invoice> findById(InvoiceId id) {
        return jpaRepository.findById(id.value()).map(InvoiceEntity::toDomain);
    }

    @Override
    public List<Invoice> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(InvoiceEntity::toDomain).toList();
    }
}

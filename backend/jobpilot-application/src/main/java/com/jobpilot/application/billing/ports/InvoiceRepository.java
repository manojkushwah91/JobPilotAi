package com.jobpilot.application.billing.ports;

import com.jobpilot.domain.billing.Invoice;
import com.jobpilot.domain.billing.InvoiceId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(InvoiceId id);
    List<Invoice> findByUserId(UUID userId);
}

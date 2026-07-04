package com.jobpilot.infrastructure.persistence.billing;

import com.jobpilot.domain.billing.Invoice;
import com.jobpilot.domain.billing.InvoiceId;
import com.jobpilot.domain.billing.SubscriptionId;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class InvoiceEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "subscription_id") private UUID subscriptionId;
    @Column(name = "amount", nullable = false) private BigDecimal amount;
    @Column(name = "currency", nullable = false) private String currency;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "description") private String description;
    @Column(name = "pdf_url") private String pdfUrl;
    @Column(name = "paid_at") private Instant paidAt;
    @Column(name = "due_at") private Instant dueAt;

    protected InvoiceEntity() {}

    public static InvoiceEntity fromDomain(Invoice inv) {
        var e = new InvoiceEntity();
        e.id = inv.invoiceId().value();
        e.userId = inv.userId();
        e.subscriptionId = inv.subscriptionId() != null ? inv.subscriptionId().value() : null;
        e.amount = inv.amount();
        e.currency = inv.currency();
        e.status = inv.status();
        e.description = inv.description();
        e.pdfUrl = inv.pdfUrl();
        e.paidAt = inv.paidAt();
        e.dueAt = inv.dueAt();
        return e;
    }

    public Invoice toDomain() {
        return Invoice.reconstitute(InvoiceId.from(id), userId,
            subscriptionId != null ? SubscriptionId.from(subscriptionId) : null,
            amount, currency, status, description, pdfUrl, paidAt, dueAt, createdAt, updatedAt);
    }
}

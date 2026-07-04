package com.jobpilot.domain.billing;

import com.jobpilot.domain.shared.BaseAggregateRoot;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Invoice extends BaseAggregateRoot {

    private InvoiceId invoiceId;
    private UUID userId;
    private SubscriptionId subscriptionId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    private String pdfUrl;
    private Instant paidAt;
    private Instant dueAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private Invoice(InvoiceId invoiceId, UUID userId, BigDecimal amount, String currency) {
        super(invoiceId.value());
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.status = "PENDING";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Invoice create(InvoiceId invoiceId, UUID userId, BigDecimal amount, String currency) {
        return new Invoice(invoiceId, userId, amount, currency);
    }

    public static Invoice reconstitute(InvoiceId invoiceId, UUID userId, SubscriptionId subscriptionId,
            BigDecimal amount, String currency, String status, String description, String pdfUrl,
            Instant paidAt, Instant dueAt, Instant createdAt, Instant updatedAt) {
        var inv = new Invoice(invoiceId, userId, amount, currency);
        inv.subscriptionId = subscriptionId;
        inv.status = status;
        inv.description = description;
        inv.pdfUrl = pdfUrl;
        inv.paidAt = paidAt;
        inv.dueAt = dueAt;
        return inv;
    }

    public void markPaid() { this.status = "PAID"; this.paidAt = Instant.now(); }
    public void markFailed() { this.status = "FAILED"; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public InvoiceId invoiceId() { return invoiceId; }
    public UUID userId() { return userId; }
    public SubscriptionId subscriptionId() { return subscriptionId; }
    public BigDecimal amount() { return amount; }
    public String currency() { return currency; }
    public String status() { return status; }
    public String description() { return description; }
    public String pdfUrl() { return pdfUrl; }
    public Instant paidAt() { return paidAt; }
    public Instant dueAt() { return dueAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}

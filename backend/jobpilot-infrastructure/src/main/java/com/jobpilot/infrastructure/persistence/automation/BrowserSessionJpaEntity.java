package com.jobpilot.infrastructure.persistence.automation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "browser_sessions")
public class BrowserSessionJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "board_name", nullable = false)
    private String boardName;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "current_url")
    private String currentUrl;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BrowserSessionJpaEntity() {}

    public BrowserSessionJpaEntity(String id, String boardName, String status) {
        this.id = id;
        this.boardName = boardName;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBoardName() { return boardName; }
    public void setBoardName(String boardName) { this.boardName = boardName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentUrl() { return currentUrl; }
    public void setCurrentUrl(String currentUrl) { this.currentUrl = currentUrl; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}

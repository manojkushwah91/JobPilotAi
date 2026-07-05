package com.jobpilot.infrastructure.persistence.automation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrowserSessionJpaRepository extends JpaRepository<BrowserSessionJpaEntity, String> {

    List<BrowserSessionJpaEntity> findByStatus(String status);

    List<BrowserSessionJpaEntity> findByBoardName(String boardName);

    @Query("SELECT s FROM BrowserSessionJpaEntity s WHERE s.status = 'ACTIVE' ORDER BY s.createdAt DESC")
    List<BrowserSessionJpaEntity> findActiveSessions();

    @Query("SELECT COUNT(s) FROM BrowserSessionJpaEntity s WHERE s.status = 'ACTIVE'")
    int countActiveSessions();
}

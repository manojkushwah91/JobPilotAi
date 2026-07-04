package com.jobpilot.domain.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AgentMemoryTest {

    private UUID testUserId;
    private AgentMemory testMemory;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testMemory = AgentMemory.create(testUserId, MemoryType.REJECTED_COMPANY, "TCS", "Poor work culture");
    }

    @Test
    void create_shouldCreateMemoryWithDefaults() {
        assertNotNull(testMemory.memoryId());
        assertEquals(testUserId, testMemory.userId());
        assertEquals(MemoryType.REJECTED_COMPANY, testMemory.memoryType());
        assertEquals("TCS", testMemory.memoryKey());
        assertEquals("Poor work culture", testMemory.value());
        assertEquals(1.0, testMemory.confidence());
        assertEquals(0, testMemory.accessCount());
        assertTrue(testMemory.isActive());
    }

    @Test
    void access_shouldIncrementAccessCount() {
        testMemory.access();
        testMemory.access();

        assertEquals(2, testMemory.accessCount());
        assertNotNull(testMemory.lastAccessedAt());
    }

    @Test
    void updateValue_shouldUpdateTheValue() {
        testMemory.updateValue("New reason");

        assertEquals("New reason", testMemory.value());
    }

    @Test
    void updateConfidence_shouldUpdateConfidence() {
        testMemory.updateConfidence(0.8);

        assertEquals(0.8, testMemory.confidence());
    }

    @Test
    void updateConfidence_shouldClampBetweenZeroAndOne() {
        testMemory.updateConfidence(1.5);
        assertEquals(1.0, testMemory.confidence());

        testMemory.updateConfidence(-0.5);
        assertEquals(0.0, testMemory.confidence());
    }

    @Test
    void deactivate_shouldSetInactive() {
        testMemory.deactivate();

        assertFalse(testMemory.isActive());
    }

    @Test
    void activate_shouldSetActive() {
        testMemory.deactivate();
        testMemory.activate();

        assertTrue(testMemory.isActive());
    }

    @Test
    void reconstitute_shouldRecreateMemoryFromData() {
        var memory = AgentMemory.reconstitute(
            testMemory.memoryId(),
            testUserId,
            MemoryType.REJECTED_COMPANY,
            "TCS",
            "Poor work culture",
            Map.of("source", "manual"),
            0.9, 5, Instant.now(),
            true, Instant.now(), Instant.now()
        );

        assertEquals(testMemory.memoryId(), memory.memoryId());
        assertEquals(0.9, memory.confidence());
        assertEquals(5, memory.accessCount());
        assertTrue(memory.isActive());
    }
}

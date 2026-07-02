package com.jobpilot.domain.shared;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    @Test
    void shouldGenerateIdOnCreation() {
        var entity = new TestEntity();
        assertThat(entity.id()).isNotNull();
    }

    @Test
    void shouldStartWithZeroVersion() {
        var entity = new TestEntity();
        assertThat(entity.version()).isZero();
    }

    @Test
    void shouldAcceptCustomId() {
        var id = UUID.randomUUID();
        var entity = new TestEntity(id);
        assertThat(entity.id()).isEqualTo(id);
    }

    @Test
    void shouldIncrementVersion() {
        var entity = new TestEntity();
        entity.doSomething();
        assertThat(entity.version()).isEqualTo(1);
    }

    @Test
    void shouldBeEqualBasedOnId() {
        var id = UUID.randomUUID();
        var e1 = new TestEntity(id);
        var e2 = new TestEntity(id);
        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentIds() {
        var e1 = new TestEntity();
        var e2 = new TestEntity();
        assertThat(e1).isNotEqualTo(e2);
    }

    @Test
    void shouldReturnSameInstanceForEquals() {
        var entity = new TestEntity();
        assertThat(entity.equals(entity)).isTrue();
    }

    @Test
    void shouldNotBeEqualToNull() {
        var entity = new TestEntity();
        assertThat(entity.equals(null)).isFalse();
    }

    static class TestEntity extends BaseEntity {
        TestEntity() { super(); }
        TestEntity(UUID id) { super(id); }

        void doSomething() {
            incrementVersion();
        }
    }
}

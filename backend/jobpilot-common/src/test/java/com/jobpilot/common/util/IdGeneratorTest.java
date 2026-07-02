package com.jobpilot.common.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorTest {

    @Test
    void shouldGenerateUniqueUuids() {
        var id1 = IdGenerator.newId();
        var id2 = IdGenerator.newId();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldGenerateStringIds() {
        var id = IdGenerator.newIdString();
        assertThat(id).isNotNull().isNotEmpty();
        assertThat(java.util.UUID.fromString(id)).isNotNull();
    }

    @Test
    void shouldParseValidUuid() {
        var uuid = java.util.UUID.randomUUID();
        var parsed = IdGenerator.fromString(uuid.toString());
        assertThat(parsed).isEqualTo(uuid);
    }
}

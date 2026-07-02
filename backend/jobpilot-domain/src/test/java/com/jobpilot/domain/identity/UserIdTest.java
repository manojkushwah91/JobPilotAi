package com.jobpilot.domain.identity;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserIdTest {

    @Test
    void shouldCreateFromUuid() {
        var uuid = UUID.randomUUID();
        var userId = UserId.from(uuid);
        assertThat(userId.value()).isEqualTo(uuid);
    }

    @Test
    void shouldGenerateNewId() {
        var id1 = UserId.generate();
        var id2 = UserId.generate();
        assertThat(id1.value()).isNotNull();
        assertThat(id2.value()).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> UserId.from(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldBeEqualForSameValue() {
        var uuid = UUID.randomUUID();
        var id1 = UserId.from(uuid);
        var id2 = UserId.from(uuid);
        assertThat(id1).isEqualTo(id2);
    }
}

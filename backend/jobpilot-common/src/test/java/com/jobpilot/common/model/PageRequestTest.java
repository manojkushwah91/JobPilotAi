package com.jobpilot.common.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PageRequestTest {

    @Test
    void shouldCreateWithDefaults() {
        var req = new PageRequest(-1, 0, null, null);
        assertThat(req.page()).isEqualTo(PageRequest.DEFAULT_PAGE);
        assertThat(req.size()).isEqualTo(PageRequest.DEFAULT_SIZE);
        assertThat(req.sort()).isEqualTo("createdAt");
        assertThat(req.direction()).isEqualTo("DESC");
    }

    @Test
    void shouldResetSizeToDefaultWhenExceedsMax() {
        var req = new PageRequest(0, 1000, "name", "ASC");
        assertThat(req.size()).isEqualTo(PageRequest.DEFAULT_SIZE);
    }

    @Test
    void shouldCreateWithOf() {
        var req = PageRequest.of(2, 50);
        assertThat(req.page()).isEqualTo(2);
        assertThat(req.size()).isEqualTo(50);
        assertThat(req.sort()).isEqualTo("createdAt");
        assertThat(req.direction()).isEqualTo("DESC");
    }

    @Test
    void shouldPreserveValidValues() {
        var req = new PageRequest(1, 25, "name", "ASC");
        assertThat(req.page()).isEqualTo(1);
        assertThat(req.size()).isEqualTo(25);
        assertThat(req.sort()).isEqualTo("name");
        assertThat(req.direction()).isEqualTo("ASC");
    }
}

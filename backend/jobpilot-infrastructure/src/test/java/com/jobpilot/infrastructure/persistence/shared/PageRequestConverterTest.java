package com.jobpilot.infrastructure.persistence.shared;

import com.jobpilot.common.model.PageRequest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PageRequestConverterTest {

    @Test
    void shouldConvertToSpringPageRequest() {
        var request = new PageRequest(1, 25, "name", "ASC");
        var spring = PageRequestConverter.toSpring(request);

        assertThat(spring.getPageNumber()).isEqualTo(1);
        assertThat(spring.getPageSize()).isEqualTo(25);
        assertThat(spring.getSort().toString()).contains("name");
    }

    @Test
    void shouldDefaultToDescending() {
        var request = new PageRequest(0, 20, "createdAt", null);
        var spring = PageRequestConverter.toSpring(request);

        assertThat(spring.getSort().isSorted()).isTrue();
    }

    @Test
    void shouldHandlePaginationBounds() {
        var request = new PageRequest(0, PageRequest.MAX_SIZE, "id", "ASC");
        var spring = PageRequestConverter.toSpring(request);

        assertThat(spring.getPageSize()).isEqualTo(PageRequest.MAX_SIZE);
    }
}

package com.jobpilot.domain.shared;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BaseValueObjectTest {

    @Test
    void shouldBeEqualForSameFields() {
        var v1 = new TestValueObject("hello", 42);
        var v2 = new TestValueObject("hello", 42);
        assertThat(v1).isEqualTo(v2);
        assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentFields() {
        var v1 = new TestValueObject("hello", 42);
        var v2 = new TestValueObject("world", 42);
        assertThat(v1).isNotEqualTo(v2);
    }

    @Test
    void shouldBeEqualForSameInstance() {
        var v1 = new TestValueObject("hello", 42);
        assertThat(v1.equals(v1)).isTrue();
    }

    @Test
    void shouldNotBeEqualToNull() {
        var v1 = new TestValueObject("hello", 42);
        assertThat(v1.equals(null)).isFalse();
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        var v1 = new TestValueObject("hello", 42);
        assertThat(v1.equals("string")).isFalse();
    }

    @Test
    void shouldHandleArrayFields() {
        var v1 = new ArrayValueObject(new String[]{"a", "b"});
        var v2 = new ArrayValueObject(new String[]{"a", "b"});
        assertThat(v1).isEqualTo(v2);
    }

    static class TestValueObject extends BaseValueObject {
        private final String name;
        private final int value;

        TestValueObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        protected Object[] equalityFields() {
            return new Object[]{name, value};
        }
    }

    static class ArrayValueObject extends BaseValueObject {
        private final String[] items;

        ArrayValueObject(String[] items) {
            this.items = items;
        }

        @Override
        protected Object[] equalityFields() {
            return new Object[]{items};
        }
    }
}

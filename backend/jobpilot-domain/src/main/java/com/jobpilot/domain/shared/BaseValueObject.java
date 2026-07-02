package com.jobpilot.domain.shared;

import java.util.Arrays;
import java.util.Objects;

public abstract class BaseValueObject {

    protected abstract Object[] equalityFields();

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return Arrays.deepEquals(equalityFields(), ((BaseValueObject) other).equalityFields());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.deepHashCode(equalityFields()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + fieldsToString() + "}";
    }

    protected String fieldsToString() {
        return Arrays.toString(equalityFields());
    }
}

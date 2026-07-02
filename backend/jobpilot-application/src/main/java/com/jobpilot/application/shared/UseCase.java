package com.jobpilot.application.shared;

public interface UseCase<I, O> {
    O execute(I input);
}

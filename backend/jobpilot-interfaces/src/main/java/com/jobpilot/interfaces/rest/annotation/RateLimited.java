package com.jobpilot.interfaces.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    int capacity() default 10;
    int refillTokens() default 3;
    int refillDurationSeconds() default 60;
    String key() default "";
}

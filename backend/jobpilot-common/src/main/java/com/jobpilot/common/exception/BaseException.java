package com.jobpilot.common.exception;

public abstract class BaseException extends RuntimeException {
    private final String code;
    private final String message;
    private final transient Object[] args;

    protected BaseException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.message = message;
        this.args = args;
    }

    protected BaseException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.args = new Object[0];
    }

    public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    public Object[] getArgs() { return args; }

    public abstract int httpStatus();
}

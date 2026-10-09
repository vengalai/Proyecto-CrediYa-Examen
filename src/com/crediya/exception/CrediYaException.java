package com.crediya.exception;

/** Business or persistence error shown to the user (unchecked to keep code simple). */
public class CrediYaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CrediYaException(String message) { super(message); }
    public CrediYaException(String message, Throwable cause) { super(message, cause); }
}

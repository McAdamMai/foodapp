package com.yourapp.exception;

/**
 * Custom exception thrown when a specific template resource cannot be found in the system.
 *
 * By extending RuntimeException, this becomes an "unchecked" exception,
 * meaning it doesn't need to be explicitly declared in method signatures.
 * This is a common practice for exceptions that will be handled globally,
 * such as by a @ControllerAdvice, to produce a specific HTTP status code (e.g., 404 Not Found).
 */
public class TemplateNotFoundException extends RuntimeException {

    /**
     * Constructor that accepts a detailed message about the exception.
     *
     * @param message The detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public TemplateNotFoundException(String message) {
        // Call the constructor of the parent class (RuntimeException)
        // to set the exception message.
        super(message);
    }

    /**
     * Constructor that accepts a message and a cause.
     * This is useful for wrapping another exception while providing a more specific context.
     *
     * @param message The detail message.
     * @param cause The cause (which is saved for later retrieval by the
     *              {@link #getCause()} method). (A null value is permitted,
     *              and indicates that the cause is nonexistent or unknown.)
     */
    public TemplateNotFoundException(String message, Throwable cause) {
        // Call the constructor of the parent class (RuntimeException)
        // to set the message and the original cause.
        super(message, cause);
    }
}
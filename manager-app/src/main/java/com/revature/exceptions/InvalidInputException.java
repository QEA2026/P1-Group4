package com.revature.exceptions;

/*
 * InvalidInputException
 *
 * Thrown when input fails validation before it reaches the
 * database — a negative id, an empty username, a malformed value.
 *
 * Distinct from ResourceNotFoundException: this means "you asked
 * something that doesn't make sense," not "what you asked for
 * doesn't exist."
 *
 * Thrown by: services, during input validation
 * Caught by: controllers, translated into HTTP 400 Bad Request
 */

public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}
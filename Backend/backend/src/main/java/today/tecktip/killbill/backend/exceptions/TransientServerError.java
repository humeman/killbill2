package today.tecktip.killbill.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Returns an HTTP 503 SERVICE UNAVAILABLE status code.
 * <p>
 * This should be used for any request in which an error which may succeed if retried occurs.
 * @author cs
 */
public class TransientServerError extends ResponseStatusException {
    /**
     * Constructs a new Transient Server Error exception.
     * <p>
     * Returns a 503.
     * @param message Reason for server error
     */
    public TransientServerError(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    /**
     * Constructs a new Transient Server Error exception with a cause.
     * <p>
     * Returns a 503.
     * @param message Reason for server error
     * @param cause Exception that caused this (not sent to user)
     */
    public TransientServerError(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }
}
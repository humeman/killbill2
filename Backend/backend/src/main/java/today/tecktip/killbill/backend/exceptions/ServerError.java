package today.tecktip.killbill.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Returns an HTTP 500 INTERNAL SERVER ERROR status code.
 * <p>
 * This should be used for any request in which an unexpected, unrecoverable error occurs.
 * @author cs
 */
public class ServerError extends ResponseStatusException {
    /**
     * Constructs a new Server Error exception.
     * <p>
     * Returns a 500.
     * @param message Reason for server error
     */
    public ServerError(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Constructs a new Server Error exception with a cause.
     * <p>
     * Returns a 500.
     * @param message Reason for server error
     * @param cause Exception that caused this (not sent to user)
     */
    public ServerError(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
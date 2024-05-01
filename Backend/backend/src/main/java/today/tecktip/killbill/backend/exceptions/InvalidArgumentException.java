package today.tecktip.killbill.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Returns an HTTP 400 BAD REQUEST status code.
 * @author cs
 */
public class InvalidArgumentException extends ResponseStatusException {
    /**
     * Constructs a new Invalid Argument exception.
     * <p>
     * Returns a 400.
     * @param message Reason for invalid argument
     */
    public InvalidArgumentException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
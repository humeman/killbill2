package today.tecktip.killbill.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Returns an HTTP 401 UNAUTHORIZED status code.
 * @author cs
 */
public class AuthenticationFailure extends ResponseStatusException {
    /**
     * Constructs a new Authentication Failure exception.
     * <p>
     * Returns a 401.
     * @param message Reason for authentication failure
     */
    public AuthenticationFailure(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
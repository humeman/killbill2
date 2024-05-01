package today.tecktip.killbill.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import today.tecktip.killbill.backend.routes.MessageBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Returns any exceptions with a {@link MessageBody} for failures.
 * @author cs
 */
@ControllerAdvice
public class ExcHandler {
    /**
     * Handles exceptions.
     * @param ex Exception to handle
     * @param request Request sent
     * @param response Expected response
     * @return Status response with {@link MessageBody} for failure
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception ex, 
                HttpServletRequest request, HttpServletResponse response) {
        System.err.println("Failure: " + ex);
        ex.printStackTrace();

        if (ex instanceof NullPointerException) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (ex instanceof MethodArgumentNotValidException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MessageBody.ofFailure(ex.getMessage()));
        }

        return ResponseEntity.status(ex instanceof ResponseStatusException ? ((ResponseStatusException) ex).getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR).body(MessageBody.ofFailure(ex.getMessage()));
    }

	/**
	 * This class should not be instantiated manually.
	 */
	public ExcHandler() { }
}
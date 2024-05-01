package today.tecktip.killbill.frontend.exceptions;

import today.tecktip.killbill.frontend.http.ResponseBody;

/**
 * Notes that the API didn't complete a request successfully.
 * @author cs
 */
public class RuntimeApiException extends RuntimeException {
    /**
     * Response body content, if it was successfully parsed.
     */
    private final ResponseBody<?> responseBody;
    
    /**
     * HTTP status code of this exception.
     */
    private final int statusCode;

    /**
     * Constructs a new API exception
     * @param body Response body received from API
     * @param statusCode HTTP status code received with response
     */
    public RuntimeApiException(final ResponseBody<?> body, final int statusCode) {
        super("Request failed (code " + statusCode + "): " +  body.getErrorReason());
        this.responseBody = body;
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new API exception where no response body is available.
     * @param reason More details about the exception
     * @param statusCode HTTP status code
     */
    public RuntimeApiException(final String reason, final int statusCode) {
        super("Request failed (code " + statusCode + "): " + reason);
        this.statusCode = statusCode;
        this.responseBody = null;
    }

    /**
     * Gets the body of the response that gave this exception.
     * @return Response body
     */
    public ResponseBody<?> getResponseBody() {
        return responseBody;
    }

    /**
     * Gets the status code for this HTTP response.
     * @return HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}

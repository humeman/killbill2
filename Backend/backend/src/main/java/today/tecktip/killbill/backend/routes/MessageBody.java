package today.tecktip.killbill.backend.routes;

/**
 * Default message body used for all API responses.
 * <p>
 * <strong>Use {@link #ofSuccess(Object)} or {@link #ofFailure(String)}. Do not construct this manually.</strong>
 * @param success True if the request was successful
 * @param data Data to be returned if successful
 * @param error Error message to send back if failed
 * @author cs
 */
public record MessageBody(Boolean success, Object data, String error) {
    /**
     * Generates a successful API response body with some data.
     * @param data Data to be sent back (serialized to JSON)
     * @return A message body to return in your API endpoints
     */
    public static MessageBody ofSuccess(Object data) {
        return new MessageBody(true, data, null);
    }
    
    /**
     * Generates a failure API response body with a reason.
     * @param error User-facing reason the API call failed
     * @return A message body to return in your API endpoints
     */
    public static MessageBody ofFailure(String error) {
        return new MessageBody(false, null, error);
    }
}
package today.tecktip.killbill.frontend.http;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base response body, representing the data received from the server.
 * @author cs
 */
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class ResponseBody<T> {
    
    private Boolean success;
    private String reason;
    private T data;

    private boolean isMutable;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * This class should not be manually instantiated.
     */
    public ResponseBody() { 
        isMutable = true;
    }

    /**
     * Prevents any future modifications to the data.
     */
    public void setImmutable() {
        isMutable = false;
    }

    /**
     * Jackson setter method for setting if the request was successful.
     * @param success True if the method was successful
     */
    @JsonProperty(value = "success", required = true)
    public void setSuccess(final boolean success) {
        if (!isMutable) throw new UnsupportedOperationException("ResponseBody objects are immutable.");
        this.success = success;
    }

    /**
     * Checks if the response was successful or not, as reported by the server.
     * @return True if successful
     */

    public boolean getSuccess() {
        return success;
    }

    /**
     * Sets the reason for any errors (if the request failed).
     * @param reason Reason the request failed
     */
    @JsonProperty(value = "error")
    public void setErrorReason(final String reason) {
        this.reason = reason;
    }

    /**
     * Gets the reason for the error if the request was not successful.
     * @return Error reason as provided by the server
     */
    public String getErrorReason() {
        if (success) throw new UnsupportedOperationException("The request was successful.");

        return reason;
    }

    /**
     * Fills in the data for this request from the raw data provided by Jackson.
     * @param data The data field with the parameterized message data type
     */
    @JsonProperty(value = "data")
    public void setData(final T data) {
        this.data = data;
    }

    /**
     * Gets the data provided in this response.
     * @return Response data object
     */
    public T getData() {
        return data;
    }

    /**
     * Parses out a ResponseBody from an HttpResponse.
     * @param <T> Type of the message's data
     * @param response String value of response body
     * @param dataClass Class of the message's data content
     * @return A parameterized ResponseBody with data as specified by T
     * @throws JsonProcessingException Unable to deserialize
     */
    public static <T> ResponseBody<T> from(final String response, Class<T> dataClass) throws JsonProcessingException, IllegalArgumentException {
        ResponseBody<T> responseBody = MAPPER.readValue(
            response,
            MAPPER.getTypeFactory().constructParametricType(ResponseBody.class, dataClass)
        );

        responseBody.setImmutable();
        return responseBody;
    }
}

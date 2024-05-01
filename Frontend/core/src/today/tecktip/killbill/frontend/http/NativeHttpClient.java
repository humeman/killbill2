package today.tecktip.killbill.frontend.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * A base HTTP client, designed to be implemented based on a platform's native clients (ie:
 * Volley, Java HttpClient). All methods are executed asynchronously.
 * @author cs
 */
public abstract class NativeHttpClient {
    /**
     * The base API url (no trailing slash).
     */
    private final String baseUrl;

    /**
     * A map of default headers for authenticating with the API.
     */
    private Map<String, String> authenticationHeaders;

    /**
     * Constructs a new Kill Bill 2 API client.
     * @param baseUrl Base URL for the API (no trailing slash).
     */
    public NativeHttpClient(final String baseUrl) {
        this.baseUrl = baseUrl;
        authenticationHeaders = Map.of();
    }

    /**
     * Configures the client's API key.
     * @param apiKey API key or null to clear
     */
    public void setApiKey(final String apiKey) {
        if (apiKey == null) this.authenticationHeaders = Map.of();
        else this.authenticationHeaders = Map.of(
            "Authorization", "Bearer " + apiKey
        );
    }

    /**
     * Returns the default authentication headers with the API key added.
     * @return Authentication headers
     */
    public Map<String, String> getAuthenticationHeaders() {
        return authenticationHeaders;
    }

    /**
     * Low-level method which requests data from the API and formats it into a ResponseBody. 
     * <strong>Prefer defined HTTP methods over this, your life will become much easier.</strong>
     * @param <T> Type of the message's "data" field
     * @param url URL extension to send GET request to
     * @param params Parameters to add to the URL
     * @param headers Message headers (see {@link #getAuthenticationHeaders()})
     * @param dataClass Class of the message's "data" field
     * @param responseBodyHandler Callback for successful responses
     * @param responseErrorHandler Callback for failed responses
     * @throws URISyntaxException Invalid URI
     */
    public abstract <T> void get(
            final String url,
            final Map<String, String> params,
            final Map<String, String> headers,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) throws URISyntaxException;

    /**
     * Low-level method which deletes data from the API and formats responses into a ResponseBody. 
     * <strong>Prefer defined HTTP methods over this, your life will become much easier.</strong>
     * @param <T> Type of the message's "data" field
     * @param url URL extension to send GET request to
     * @param params Parameters to add to the URL
     * @param headers Message headers (see {@link #getAuthenticationHeaders()})
     * @param dataClass Class of the message's "data" field
     * @param responseBodyHandler Callback for successful responses
     * @param responseErrorHandler Callback for failed responses
     * @throws URISyntaxException Invalid URI
     */
    public abstract <T> void delete(
            final String url,
            final Map<String, String> params,
            final Map<String, String> headers,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) throws URISyntaxException;

    /**
     * Low-level method which sends data from the API and formats responses into a ResponseBody. 
     * <strong>Prefer defined HTTP methods over this, your life will become much easier.</strong>
     * @param <T> Type of the message's "data" field
     * @param url URL extension to send GET request to
     * @param requestBody JSON serializable object of the message being sent
     * @param headers Message headers (see {@link #getAuthenticationHeaders()})
     * @param dataClass Class of the message's "data" field
     * @param responseBodyHandler Callback for successful responses
     * @param responseErrorHandler Callback for failed responses
     * @throws URISyntaxException Invalid URI
     * @throws JsonProcessingException Unable to serialize body as JSON
     */
    public abstract <T> void put(
            final String url,
            final Object requestBody,
            final Map<String, String> headers,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) throws URISyntaxException, JsonProcessingException;

    /**
     * Low-level method which sends data from the API and formats responses into a ResponseBody. 
     * <strong>Prefer defined HTTP methods over this, your life will become much easier.</strong>
     * @param <T> Type of the message's "data" field
     * @param url URL extension to send GET request to
     * @param requestBody JSON serializable object of the message being sent
     * @param headers Message headers (see {@link #getAuthenticationHeaders()})
     * @param dataClass Class of the message's "data" field
     * @param responseBodyHandler Callback for successful responses
     * @param responseErrorHandler Callback for failed responses
     * @throws URISyntaxException Invalid URI
     * @throws JsonProcessingException Unable to serialize body as JSON
     */
    public abstract <T> void post(
            final String url,
            final Object requestBody,
            final Map<String, String> headers,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) throws URISyntaxException, JsonProcessingException;

    /**
     * Converts a relative URL and some parameters into an absolute URL-encoded URI.
     * @param url Relative URL (from {@link #baseUrl})
     * @param params Any parameters to URL encode
     * @return An encoded URI
     * @throws URISyntaxException Generated URI is not valid
     */
    public URI getUrlWithParams(final String url, final Map<String, String> params) throws URISyntaxException {
        String realUrl = url.charAt(0) == '/' ? url : "/" + url;
        StringBuilder compUrl = new StringBuilder(baseUrl + realUrl);

        if (params.size() > 0) {
            compUrl.append("?");

            for (Map.Entry<String, String> entry : params.entrySet()) {
                compUrl.append(URLEncoder.encode(entry.getKey(), Charset.defaultCharset()));
                compUrl.append("=");
                compUrl.append(URLEncoder.encode(entry.getValue(), Charset.defaultCharset()));
                compUrl.append("&");
            }
            compUrl.deleteCharAt(compUrl.length() - 1);
        }

        return new URI(compUrl.toString());
    }

    /**
     * Generates an absolute URL with the concatenation of a specified url with the {@link #baseUrl}.
     * @param url URL beyond the {@link #baseUrl}. Leading / optional.
     * @return An encoded URI
     * @throws URISyntaxException Generated URI is not valid
     */
    public URI getUrl(final String url) throws URISyntaxException {
        String realUrl = url.charAt(0) == '/' ? url : "/" + url;

        return new URI(baseUrl + realUrl);
    }

    /**
     * A generic response body handler, which is called when the method completes successfully.
     */
    public interface ResponseBodyHandler<T> {
        /**
         * Handle a response body.
         * @param body Response body
         */
        void handle(final ResponseBody<T> body);
    }

    /**
     * A generic response error handler, which is called when the method fails during sending or parsing or the API returns an error.
     */
    public interface ResponseErrorHandler {
        /**
         * Handle a response body.
         * @param cause Reason the request failed
         */
        void handle(final Throwable cause);
    }
}

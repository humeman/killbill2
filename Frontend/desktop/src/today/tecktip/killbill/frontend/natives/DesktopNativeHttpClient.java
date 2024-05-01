package today.tecktip.killbill.frontend.natives;

import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.exceptions.RuntimeApiException;
import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.http.ResponseBody;

/**
 * A native implementation of the HTTP client internally using Java's {@link HttpClient}.
 * @author cs
 */
public class DesktopNativeHttpClient extends NativeHttpClient {
    /**
     * The HTTP client requests are sent through.
     */
    private final HttpClient client;

    /**
     * Jackson object mapper for JSON serialization.
     */
    private final ObjectMapper mapper;

    /**
     * Constructs a new desktop Kill Bill 2 API client.
     * @param baseUrl Base URL for the API (no trailing slash).
     */
    public DesktopNativeHttpClient(final String baseUrl) {
        super(baseUrl);
        client = HttpClient.newBuilder()
            .proxy(ProxySelector.getDefault())
            .build(); // May need modifications if we add SSL or anything in the future
        mapper = new ObjectMapper();
    }

    /**
     * Appends each header in a Map to the specified request builder. 
     * @param builder Request builder to add headers to
     * @param headers Map of headers
     */
    private void addHeaders(final HttpRequest.Builder builder, final Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public <T> void get(
            final String url,
            final Map<String, String> params,
            final Map<String, String> headers,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) throws URISyntaxException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(getUrlWithParams(url, params))
            .GET();

        addHeaders(requestBuilder, headers);

        HttpRequest request = requestBuilder.build();

        completeRequest(request, dataClass, responseBodyHandler, responseErrorHandler);
    }

    @Override
    public <T> void delete(
            final String url,
            final Map<String, String> params,
            final Map<String, String> headers,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) throws URISyntaxException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(getUrlWithParams(url, params))
            .DELETE();

        addHeaders(requestBuilder, headers);

        HttpRequest request = requestBuilder.build();

        completeRequest(request, dataClass, responseBodyHandler, responseErrorHandler);
    }

    @Override
    public <T> void put(
            final String url,
            final Object requestBody,
            final Map<String, String> headers,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) throws URISyntaxException, JsonProcessingException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(getUrl(url))
            .header("Content-Type", "application/json")
            .PUT(BodyPublishers.ofString(mapper.writeValueAsString(requestBody)));

        addHeaders(requestBuilder, headers);

        HttpRequest request = requestBuilder.build();

        completeRequest(request, dataClass, responseBodyHandler, responseErrorHandler);
    }

    @Override
    public <T> void post(
            final String url,
            final Object requestBody,
            final Map<String, String> headers,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) throws URISyntaxException, JsonProcessingException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(getUrl(url))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(mapper.writeValueAsString(requestBody)));

        addHeaders(requestBuilder, headers);

        HttpRequest request = requestBuilder.build();

        completeRequest(request, dataClass, responseBodyHandler, responseErrorHandler);
    }

    /**
     * Sends a request and parses/validates the response body, passing it along to handler methods.
     * @param <T> Type of the response's data field
     * @param request Request to be sent
     * @param dataClass Data class of the response's data field
     * @param responseBodyHandler Lambda method which handles the response, when completed.
     *                            Called asynchronously.
     * @param responseErrorHandler Lambda method which handles any errors, when found.
     *                             Called asynchronously.
     *                             Throws RuntimeApiException plus any exceptions thrown during
     *                             send.
     */
    private <T> void completeRequest(
            final HttpRequest request,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) {
        client.sendAsync(request, BodyHandlers.ofString(Charset.defaultCharset()))
            .thenAccept(httpResponse -> {
                final ResponseBody<T> responseBody;
                try {
                    responseBody = ResponseBody.from(httpResponse.body(), dataClass);
                } catch (final Throwable t) {
                    if (httpResponse.statusCode() != 200) {
                        // Assume this is an error that has no deserializable data (ex: 404)
                        throw new RuntimeApiException("Unable to deserialize response body.", httpResponse.statusCode());
                    }

                    throw new CatastrophicException("Unable to deserialize API response.", t);
                }
                if (httpResponse.statusCode() != 200) {
                    // Assume this is an error that has no deserializable data (ex: 404)
                    throw new RuntimeApiException("Error response: " + httpResponse.statusCode(), httpResponse.statusCode());
                } else {
                    responseBodyHandler.handle(responseBody);
                }
            })
            .exceptionally(t -> {
                responseErrorHandler.handle(t);
                return null;
            });
    }
}

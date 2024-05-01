package today.tecktip.killbill.frontend.natives;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import today.tecktip.killbill.frontend.exceptions.ApiException;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.http.NativeHttpClient;
import today.tecktip.killbill.frontend.http.ResponseBody;

/**
 * A native implementation of the HTTP client internally using Android Volley.
 * @author cs
 */
public class AndroidNativeHttpClient extends NativeHttpClient {
    /**
     * This is a temporary static reference for sharing between Android activities.
     * In our GDX implementation, this will not exist.
     */
    public static AndroidNativeHttpClient client;


    /**
     * The Volley request queue in use.
     */
    private final RequestQueue queue;

    /**
     * A Jackson object mapper for serializing response bodies.
     */
    private final ObjectMapper mapper;

    /**
     * Constructs a new desktop Kill Bill 2 API client with an Android Volley implementation.
     * @param baseUrl Base URL for the API (no trailing slash).
     * @param applicationContext Android application context to attach to request queue.
     */
    public AndroidNativeHttpClient(final String baseUrl, final Context applicationContext) {
        super(baseUrl);
        queue = Volley.newRequestQueue(applicationContext);
        mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
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
        StringRequest request = new StringRequest(Request.Method.GET, getUrlWithParams(url, params).toString(),
                response -> {
                    // Parse out response into JSON
                    final ResponseBody<T> responseBody;
                    try {
                        responseBody = parseResponse(response, dataClass);
                    } catch (final IOException e) {
                        responseErrorHandler.handle(e);
                        return;
                    } try {
                        responseBodyHandler.handle(responseBody);
                    } catch (final Throwable t) {
                        responseErrorHandler.handle(new RuntimeException("Error in response processing", t));
                    }
                },
                error -> {
                    error.printStackTrace();
                    if (error.networkResponse != null && error.networkResponse.statusCode != 200) {
                        // Check if there's a body
                        final ResponseBody<T> responseBody;
                        try {
                            responseBody = parseResponse(new String(error.networkResponse.data), dataClass);
                        } catch (final IOException e) {
                            responseErrorHandler.handle(new ApiException(error.getMessage(), error.networkResponse.statusCode));
                            return;
                        }

                        responseErrorHandler.handle(new ApiException(responseBody, error.networkResponse.statusCode));
                    } else {
                        responseErrorHandler.handle(new ApiException(error.getMessage(), -1));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> newHeaders = new HashMap<>(headers);
                newHeaders.put("Content-Type", "application/json");
                return newHeaders;
            }
        };

        queue.add(request);
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
        StringRequest request = new StringRequest(Request.Method.DELETE, getUrlWithParams(url, params).toString(),
                response -> {
                    // Parse out response into JSON
                    final ResponseBody<T> responseBody;
                    try {
                        responseBody = parseResponse(response, dataClass);
                    } catch (final IOException e) {
                        responseErrorHandler.handle(e);
                        return;
                    } try {
                        responseBodyHandler.handle(responseBody);
                    } catch (final Throwable t) {
                        responseErrorHandler.handle(new RuntimeException("Error in response processing", t));
                    }
                },
                error -> {
                    error.printStackTrace();
                    if (error.networkResponse != null && error.networkResponse.statusCode != 200) {
                        // Check if there's a body
                        final ResponseBody<T> responseBody;
                        try {
                            responseBody = parseResponse(new String(error.networkResponse.data), dataClass);
                        } catch (final IOException e) {
                            responseErrorHandler.handle(new ApiException(error.getMessage(), error.networkResponse.statusCode));
                            return;
                        }

                        responseErrorHandler.handle(new ApiException(responseBody, error.networkResponse.statusCode));
                    } else {
                        responseErrorHandler.handle(new ApiException(error.getMessage(), -1));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> newHeaders = new HashMap<>(headers);
                newHeaders.put("Content-Type", "application/json");
                return newHeaders;
            }
        };

        queue.add(request);
    }

    @Override
    public <T> void put(
            final String url,
            final Object requestBody,
            final Map<String, String> headers,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) throws URISyntaxException {
        StringRequest request = new StringRequest(Request.Method.PUT, getUrl(url).toString(),
                response -> {
                    // Parse out response into JSON
                    final ResponseBody<T> responseBody;
                    try {
                        responseBody = parseResponse(response, dataClass);
                    } catch (final IOException e) {
                        responseErrorHandler.handle(e);
                        return;
                    } try {
                        responseBodyHandler.handle(responseBody);
                    } catch (final Throwable t) {
                        responseErrorHandler.handle(new RuntimeException("Error in response processing", t));
                    }
                },
                error -> {
                    error.printStackTrace();
                    if (error.networkResponse != null && error.networkResponse.statusCode != 200) {
                        // Check if there's a body
                        final ResponseBody<T> responseBody;
                        try {
                            responseBody = parseResponse(new String(error.networkResponse.data), dataClass);
                        } catch (final IOException e) {
                            responseErrorHandler.handle(new ApiException(error.getMessage(), error.networkResponse.statusCode));
                            return;
                        }

                        responseErrorHandler.handle(new ApiException(responseBody, error.networkResponse.statusCode));
                    } else {
                        responseErrorHandler.handle(new ApiException(error.getMessage(), -1));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> newHeaders = new HashMap<>(headers);
                newHeaders.put("Content-Type", "application/json");
                return newHeaders;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                try {
                    return mapper.writeValueAsBytes(requestBody);
                } catch (final Throwable t) {
                    throw new CatastrophicException("Failed to map request body.", t);
                }
            }
        };

        queue.add(request);
    }

    @Override
    public <T> void post(
            final String url,
            final Object requestBody,
            final Map<String, String> headers,
            final Class<T> dataClass,
            final ResponseBodyHandler<T> responseBodyHandler,
            final ResponseErrorHandler responseErrorHandler
    ) throws URISyntaxException {
        StringRequest request = new StringRequest(Request.Method.POST, getUrl(url).toString(),
                response -> {
                    // Parse out response into JSON
                    final ResponseBody<T> responseBody;
                    try {
                        responseBody = parseResponse(response, dataClass);
                    } catch (final IOException e) {
                        responseErrorHandler.handle(e);
                        return;
                    } try {
                        responseBodyHandler.handle(responseBody);
                    } catch (final Throwable t) {
                        responseErrorHandler.handle(new RuntimeException("Error in response processing", t));
                    }
                },
                error -> {
                    error.printStackTrace();
                    if (error.networkResponse != null && error.networkResponse.statusCode != 200) {
                        // Check if there's a body
                        final ResponseBody<T> responseBody;
                        try {
                            responseBody = parseResponse(new String(error.networkResponse.data), dataClass);
                        } catch (final IOException e) {
                            responseErrorHandler.handle(new ApiException(error.getMessage(), error.networkResponse.statusCode));
                            return;
                        }

                        responseErrorHandler.handle(new ApiException(responseBody, error.networkResponse.statusCode));
                    } else {
                        responseErrorHandler.handle(new ApiException(error.getMessage(), -1));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> newHeaders = new HashMap<>(headers);
                newHeaders.put("Content-Type", "application/json");
                return newHeaders;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                try {
                    return mapper.writeValueAsBytes(requestBody);
                } catch (final Throwable t) {
                    throw new CatastrophicException("Failed to map request body.", t);
                }
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    /**
     * Sends a request and parses/validates the response body.
     * @param <T> Type of the response's data field
     * @param response Response content string
     * @return Parsed response body
     * @throws IOException Unable to send request to API
     * @throws ApiException API returned an error
     */
    private <T> ResponseBody<T> parseResponse(final String response, final Class<T> dataClass) throws IOException {
        final ResponseBody<T> responseBody;
        try {
            responseBody = ResponseBody.from(response, dataClass);
        } catch (final Throwable t) {
            throw new CatastrophicException("Unable to deserialize API response.", t);
        }

        return responseBody;
    }
}

package com.apiforge.studio.service;

import com.apiforge.studio.model.ApiRequest;
import com.apiforge.studio.model.ApiResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class HttpClientService {
    private final AuthEngine authEngine;
    private final ScriptEngineService scriptEngine;

    public HttpClientService() {
        this.authEngine = new AuthEngine();
        this.scriptEngine = new ScriptEngineService();
    }

    private HttpClient buildClient(ApiRequest request) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10));
        
        boolean followRedirects = true;
        boolean verifySsl = true;
        
        if (request.getRequestSettings() != null) {
            followRedirects = request.getRequestSettings().getOrDefault("followRedirects", true);
            verifySsl = request.getRequestSettings().getOrDefault("verifySsl", true);
        }
        
        builder.followRedirects(followRedirects ? HttpClient.Redirect.ALWAYS : HttpClient.Redirect.NEVER);
        
        if (!verifySsl) {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                    }
                };
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                builder.sslContext(sc);
            } catch (Exception e) {
                // Ignore
            }
        }
        return builder.build();
    }

    private HttpRequest.BodyPublisher buildUrlEncodedBody(Map<String, String> formData, Map<String, String> env) {
        StringBuilder builder = new StringBuilder();
        if (formData != null) {
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                if (builder.length() > 0) builder.append("&");
                builder.append(URLEncoder.encode(resolveVariables(entry.getKey(), env), StandardCharsets.UTF_8))
                       .append("=")
                       .append(URLEncoder.encode(resolveVariables(entry.getValue(), env), StandardCharsets.UTF_8));
            }
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    private HttpRequest.BodyPublisher buildMultipartBody(Map<String, String> formData, String boundary, Map<String, String> env) {
        StringBuilder builder = new StringBuilder();
        if (formData != null) {
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                builder.append("--").append(boundary).append("\r\n")
                       .append("Content-Disposition: form-data; name=\"")
                       .append(resolveVariables(entry.getKey(), env)).append("\"\r\n\r\n")
                       .append(resolveVariables(entry.getValue(), env)).append("\r\n");
            }
        }
        builder.append("--").append(boundary).append("--\r\n");
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    private String resolveVariables(String text, Map<String, String> environment) {
        if (text == null || environment == null) return text;
        String resolved = text;
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            resolved = resolved.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return resolved;
    }

    public ApiResponse execute(ApiRequest request, Map<String, String> currentEnvironment) {
        HttpClient client = buildClient(request);
        long startTime = System.currentTimeMillis();
        try {
            // Stage 6: Run Pre-request Script
            try {
                scriptEngine.executePreRequestScript(request.getPreRequestScript(), request, currentEnvironment);
            } catch (Exception e) {
                return createErrorResponse(startTime, "Pre-request script failed: " + e.getMessage());
            }

            // Stage 3 & 4: Resolve Variables for URL
            String resolvedUrl = resolveVariables(request.getUrl().trim(), currentEnvironment);

            StringBuilder urlBuilder = new StringBuilder(resolvedUrl);
            if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
                if (!urlBuilder.toString().contains("?")) {
                    urlBuilder.append("?");
                } else if (!urlBuilder.toString().endsWith("&")) {
                    urlBuilder.append("&");
                }
                for (Map.Entry<String, String> entry : request.getQueryParams().entrySet()) {
                    String resolvedKey = resolveVariables(entry.getKey(), currentEnvironment);
                    String resolvedValue = resolveVariables(entry.getValue(), currentEnvironment);
                    urlBuilder.append(URLEncoder.encode(resolvedKey, StandardCharsets.UTF_8))
                            .append("=")
                            .append(URLEncoder.encode(resolvedValue, StandardCharsets.UTF_8))
                            .append("&");
                }
                if (urlBuilder.toString().endsWith("&")) {
                    urlBuilder.setLength(urlBuilder.length() - 1);
                }
            }

            URI uri = new URI(urlBuilder.toString());
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);

            Map<String, String> resolvedHeaders = new HashMap<>();
            
            // Resolve Variables for Headers
            if (request.getHeaders() != null) {
                for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                    if (entry.getKey() != null && !entry.getKey().isBlank()) {
                        resolvedHeaders.put(
                            resolveVariables(entry.getKey(), currentEnvironment),
                            resolveVariables(entry.getValue(), currentEnvironment)
                        );
                    }
                }
            }

            // Stage 5: Apply Authentication
            authEngine.applyAuth(request, resolvedHeaders);

            // Add Headers to request
            for (Map.Entry<String, String> header : resolvedHeaders.entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }

            // Apply Method & Body
            String method = request.getMethod().toUpperCase();
            HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
            if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE")) {
                String bType = request.getBodyType();
                if ("none".equals(bType)) {
                    bodyPublisher = HttpRequest.BodyPublishers.noBody();
                } else if ("form-data".equals(bType)) {
                    String boundary = "Boundary-" + System.currentTimeMillis();
                    resolvedHeaders.put("Content-Type", "multipart/form-data; boundary=" + boundary);
                    builder.header("Content-Type", "multipart/form-data; boundary=" + boundary);
                    bodyPublisher = buildMultipartBody(request.getFormData(), boundary, currentEnvironment);
                } else if ("urlencoded".equals(bType)) {
                    resolvedHeaders.put("Content-Type", "application/x-www-form-urlencoded");
                    builder.header("Content-Type", "application/x-www-form-urlencoded");
                    bodyPublisher = buildUrlEncodedBody(request.getFormData(), currentEnvironment);
                } else if ("binary".equals(bType)) {
                    bodyPublisher = HttpRequest.BodyPublishers.noBody();
                } else {
                    if (request.getBody() != null) {
                        String resolvedBody = resolveVariables(request.getBody(), currentEnvironment);
                        bodyPublisher = HttpRequest.BodyPublishers.ofString(resolvedBody);
                    }
                }
            }
            builder.method(method, bodyPublisher);

            // Execute Request
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();

            // Build ApiResponse
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatusCode(response.statusCode());
            apiResponse.setStatusMessage("OK");
            apiResponse.setResponseTimeMs(endTime - startTime);
            apiResponse.setBody(response.body());
            
            long size = response.body() != null ? response.body().getBytes(StandardCharsets.UTF_8).length : 0;
            apiResponse.setResponseSize(size);

            response.headers().map().forEach((key, list) -> {
                if (!list.isEmpty()) {
                    apiResponse.getHeaders().put(key, String.join(", ", list));
                }
            });

            // Stage 9: Run Tests
            try {
                scriptEngine.executeTestScript(request.getTestScript(), request, apiResponse, currentEnvironment);
            } catch (Exception e) {
                System.err.println("Test script failed: " + e.getMessage());
                // Non-blocking error, keep response
            }

            return apiResponse;
        } catch (Exception e) {
            return createErrorResponse(startTime, e.getMessage());
        }
    }

    private ApiResponse createErrorResponse(long startTime, String errorMsg) {
        long endTime = System.currentTimeMillis();
        ApiResponse errorResponse = ApiResponse.error(errorMsg);
        errorResponse.setResponseTimeMs(endTime - startTime);
        errorResponse.setStatusCode(0);
        errorResponse.setStatusMessage("Error");
        return errorResponse;
    }

    public void executeSse(ApiRequest request, Consumer<String> eventConsumer, Consumer<Throwable> errorConsumer) {
        new Thread(() -> {
            try {
                StringBuilder urlBuilder = new StringBuilder(request.getUrl().trim());
                URI uri = new URI(urlBuilder.toString());
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Accept", "text/event-stream");

                if (request.getHeaders() != null) {
                    request.getHeaders().forEach(builder::header);
                }
                
                Map<String, String> headers = new HashMap<>();
                authEngine.applyAuth(request, headers);
                headers.forEach(builder::header);

                HttpClient client = buildClient(request);
                HttpResponse<java.util.stream.Stream<String>> response = client.send(
                        builder.build(),
                        HttpResponse.BodyHandlers.ofLines()
                );

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    response.body().forEach(eventConsumer);
                } else {
                    errorConsumer.accept(new Exception("SSE Connection failed with status: " + response.statusCode()));
                }
            } catch (Exception e) {
                errorConsumer.accept(e);
            }
        }).start();
    }
}

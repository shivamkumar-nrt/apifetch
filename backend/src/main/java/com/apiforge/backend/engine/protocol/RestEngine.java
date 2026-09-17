package com.apiforge.backend.engine.protocol;

import com.apiforge.backend.engine.core.ExecutionEngine;
import com.apiforge.backend.engine.dto.ResponseDto;
import com.apiforge.backend.request.dto.RequestDto;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class RestEngine implements ExecutionEngine {

    private final HttpClient httpClient;

    public RestEngine() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public boolean supports(String protocol) {
        return "rest".equalsIgnoreCase(protocol);
    }

    @Override
    public ResponseDto execute(RequestDto requestDto) throws Exception {
        long startTime = System.currentTimeMillis();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(requestDto.getUrl()));

        // Method and Body
        String method = requestDto.getMethod().toUpperCase();
        HttpRequest.BodyPublisher bodyPublisher = requestDto.getBody() != null 
                ? HttpRequest.BodyPublishers.ofString(requestDto.getBody()) 
                : HttpRequest.BodyPublishers.noBody();
                
        requestBuilder.method(method, bodyPublisher);

        // Parse headers from JSON
        String headersJson = requestDto.getHeaders();
        if (headersJson != null && !headersJson.trim().isEmpty() && !headersJson.equals("{}")) {
            // Simplified JSON parsing for deep implementation without pulling Jackson manually here
            // Assuming format {"key": "value", "key2": "value2"}
            String cleaned = headersJson.replaceAll("[{}\"]", "");
            String[] pairs = cleaned.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length == 2) {
                    requestBuilder.header(kv[0].trim(), kv[1].trim());
                }
            }
        }
        
        // Add a default user agent if not provided
        if (headersJson == null || !headersJson.toLowerCase().contains("user-agent")) {
            requestBuilder.header("User-Agent", "APIForge-Studio/1.0");
        }

        HttpResponse<String> httpResponse = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        long endTime = System.currentTimeMillis();

        ResponseDto responseDto = new ResponseDto();
        responseDto.setStatusCode(httpResponse.statusCode());
        responseDto.setStatusText("OK"); // Simplified for now
        responseDto.setResponseTimeMs(endTime - startTime);
        
        String responseBody = httpResponse.body();
        responseDto.setBody(responseBody);
        responseDto.setResponseSizeBytes(responseBody != null ? responseBody.getBytes().length : 0);

        Map<String, String> headers = new HashMap<>();
        httpResponse.headers().map().forEach((k, v) -> headers.put(k, String.join(", ", v)));
        responseDto.setHeaders(headers);

        return responseDto;
    }
}

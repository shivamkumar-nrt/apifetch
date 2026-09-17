package com.apiforge.studio.utils;

import com.apiforge.studio.model.ApiRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurlParser {
    public static ApiRequest parse(String curlCmd) {
        ApiRequest req = new ApiRequest();
        req.setId(null);
        req.setMethod("GET");
        req.setProtocol("REST");
        
        if (curlCmd == null || curlCmd.trim().isEmpty()) return req;
        
        // Very basic parsing for demo. Needs robust regex or manual parsing.
        // Strip out line continuations
        curlCmd = curlCmd.replace("\\\n", " ").replace("\\\r", " ");
        
        // Extract URL
        Matcher urlMatcher = Pattern.compile("'(https?://[^']+)'|\"(https?://[^\"]+)\"|(https?://[^ ]+)").matcher(curlCmd);
        if (urlMatcher.find()) {
            String url = urlMatcher.group(1) != null ? urlMatcher.group(1) : (urlMatcher.group(2) != null ? urlMatcher.group(2) : urlMatcher.group(3));
            req.setUrl(url);
        }
        
        // Extract Method
        Matcher methodMatcher = Pattern.compile("-X\\s+([A-Z]+)|--request\\s+([A-Z]+)").matcher(curlCmd);
        if (methodMatcher.find()) {
            req.setMethod(methodMatcher.group(1) != null ? methodMatcher.group(1) : methodMatcher.group(2));
        }
        
        // Extract Headers
        Map<String, String> headers = new HashMap<>();
        Matcher headerMatcher = Pattern.compile("-H\\s+'([^']+)'|-H\\s+\"([^\"]+)\"|--header\\s+'([^']+)'|--header\\s+\"([^\"]+)\"").matcher(curlCmd);
        while (headerMatcher.find()) {
            String h = null;
            for(int i=1; i<=4; i++) { if(headerMatcher.group(i) != null) h = headerMatcher.group(i); }
            if (h != null && h.contains(":")) {
                int idx = h.indexOf(":");
                headers.put(h.substring(0, idx).trim(), h.substring(idx + 1).trim());
            }
        }
        req.setHeaders(headers);
        
        // Extract Body
        Matcher bodyMatcher = Pattern.compile("-d\\s+'([^']+)'|-d\\s+\"([^\"]+)\"|--data\\s+'([^']+)'|--data\\s+\"([^\"]+)\"|--data-raw\\s+'([^']+)'|--data-raw\\s+\"([^\"]+)\"").matcher(curlCmd);
        if (bodyMatcher.find()) {
            String body = null;
            for(int i=1; i<=6; i++) { if(bodyMatcher.group(i) != null) body = bodyMatcher.group(i); }
            if (body != null) {
                req.setBody(body);
                req.setBodyType("raw");
                if (req.getMethod().equals("GET")) {
                    req.setMethod("POST"); // curl defaults to POST if -d is present
                }
            }
        }
        
        // Basic Auth
        Matcher authMatcher = Pattern.compile("-u\\s+'([^']+)'|-u\\s+\"([^\"]+)\"|-u\\s+([^ ]+)").matcher(curlCmd);
        if (authMatcher.find()) {
            String auth = authMatcher.group(1) != null ? authMatcher.group(1) : (authMatcher.group(2) != null ? authMatcher.group(2) : authMatcher.group(3));
            if (auth != null && auth.contains(":")) {
                int idx = auth.indexOf(":");
                req.setAuthType("Basic Auth");
                req.setAuthUsername(auth.substring(0, idx));
                req.setAuthPassword(auth.substring(idx + 1));
            }
        }
        
        return req;
    }
    
    public static String generate(ApiRequest req) {
        StringBuilder sb = new StringBuilder("curl -X ");
        sb.append(req.getMethod() != null ? req.getMethod() : "GET").append(" \\\n");
        sb.append("  '").append(req.getUrl() != null ? req.getUrl() : "").append("'");
        
        if (req.getHeaders() != null) {
            for (Map.Entry<String, String> entry : req.getHeaders().entrySet()) {
                sb.append(" \\\n  -H '").append(entry.getKey()).append(": ").append(entry.getValue()).append("'");
            }
        }
        
        if ("Basic Auth".equals(req.getAuthType())) {
            sb.append(" \\\n  -u '").append(req.getAuthUsername()).append(":").append(req.getAuthPassword()).append("'");
        } else if ("Bearer Token".equals(req.getAuthType())) {
            sb.append(" \\\n  -H 'Authorization: Bearer ").append(req.getAuthToken()).append("'");
        }
        
        if (req.getBody() != null && !req.getBody().isEmpty() && !"none".equals(req.getBodyType())) {
            sb.append(" \\\n  --data-raw '").append(req.getBody().replace("'", "'\\''")).append("'");
        }
        
        return sb.toString();
    }
}

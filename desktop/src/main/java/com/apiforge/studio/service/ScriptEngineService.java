package com.apiforge.studio.service;

import com.apiforge.studio.model.ApiRequest;
import com.apiforge.studio.model.ApiResponse;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ScriptEngineService {

    private static final int TIMEOUT_SECONDS = 5;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void executePreRequestScript(String script, ApiRequest request, Map<String, String> currentEnvironment) throws Exception {
        if (script == null || script.trim().isEmpty()) {
            return;
        }

        executeWithTimeout(() -> {
            try (Context context = Context.newBuilder("js")
                    .allowHostAccess(HostAccess.EXPLICIT)
                    .allowExperimentalOptions(true)
                    .option("js.nashorn-compat", "true")
                    .build()) {

                // Setup PM API
                PostmanApi pm = new PostmanApi(request, null, currentEnvironment);
                context.getBindings("js").putMember("pm", pm);

                context.eval("js", script);
            }
            return null;
        });
    }

    public void executeTestScript(String script, ApiRequest request, ApiResponse response, Map<String, String> currentEnvironment) throws Exception {
        if (script == null || script.trim().isEmpty()) {
            return;
        }

        executeWithTimeout(() -> {
            try (Context context = Context.newBuilder("js")
                    .allowHostAccess(HostAccess.EXPLICIT)
                    .allowExperimentalOptions(true)
                    .option("js.nashorn-compat", "true")
                    .build()) {

                // Setup PM API
                PostmanApi pm = new PostmanApi(request, response, currentEnvironment);
                context.getBindings("js").putMember("pm", pm);

                // Add basic Chai-like expect mock for pm.expect
                context.eval("js", 
                    "pm.test = function(name, fn) { try { fn(); pm._addTestResult(name, true, ''); } catch(e) { pm._addTestResult(name, false, e.message); } }; " +
                    "pm.expect = function(val) { return { " +
                    "  to: { " +
                    "    be: { " +
                    "       ok: function() { if(!val) throw new Error('expected false to be ok'); }, " +
                    "       a: function(type) { if(typeof val !== type) throw new Error('expected ' + typeof val + ' to be a ' + type); } " +
                    "    }, " +
                    "    have: { " +
                    "       status: function(code) { if(val.code !== code) throw new Error('expected response to have status code ' + code + ' but got ' + val.code); } " +
                    "    } " +
                    "  } " +
                    "}; };"
                );

                context.eval("js", script);
            }
            return null;
        });
    }

    private void executeWithTimeout(java.util.concurrent.Callable<Void> task) throws Exception {
        Future<Void> future = executor.submit(task);
        try {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new Exception("Script execution timed out after " + TIMEOUT_SECONDS + " seconds.");
        }
    }

    public static class PostmanApi {
        private final ApiRequest request;
        private final ApiResponse response;
        private final Map<String, String> environment;
        private final Map<String, String> variables = new HashMap<>();
        public final EnvironmentApi environmentApi;
        public final ResponseApi responseApi;

        public PostmanApi(ApiRequest req, ApiResponse res, Map<String, String> env) {
            this.request = req;
            this.response = res;
            this.environment = env != null ? env : new HashMap<>();
            this.environmentApi = new EnvironmentApi(this.environment);
            this.responseApi = res != null ? new ResponseApi(res) : null;
        }

        @HostAccess.Export
        public EnvironmentApi getEnvironment() { return environmentApi; }

        @HostAccess.Export
        public ResponseApi getResponse() { return responseApi; }

        @HostAccess.Export
        public void _addTestResult(String name, boolean passed, String error) {
            if (response != null) {
                System.out.println("TEST [" + (passed ? "PASS" : "FAIL") + "]: " + name + (passed ? "" : " - " + error));
                // Call backend API to save result
                try {
                    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                    java.util.UUID executionId = java.util.UUID.randomUUID();
                    String requestBody = String.format(
                        "{\"requestId\":\"%s\",\"executionId\":\"%s\",\"assertionType\":\"%s\",\"passed\":%b,\"expected\":\"\",\"actual\":\"%s\"}",
                        request.getId() != null ? request.getId().toString() : java.util.UUID.randomUUID().toString(),
                        executionId.toString(),
                        name,
                        passed,
                        error != null ? error.replace("\"", "\\\"").replace("\n", "\\n") : ""
                    );
                    
                    java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://localhost:8082/api/v1/test-results"))
                            .header("Content-Type", "application/json")
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();
                            
                    client.sendAsync(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                } catch (Exception e) {
                    System.err.println("Failed to send test result to backend: " + e.getMessage());
                }
            }
        }

        public class EnvironmentApi {
            private final Map<String, String> env;
            public EnvironmentApi(Map<String, String> env) { this.env = env; }
            
            @HostAccess.Export
            public String get(String key) { return env.get(key); }
            
            @HostAccess.Export
            public void set(String key, String value) { env.put(key, value); }
            
            @HostAccess.Export
            public void unset(String key) { env.remove(key); }
        }

        public class ResponseApi {
            private final ApiResponse res;
            public ResponseApi(ApiResponse res) { this.res = res; }

            @HostAccess.Export
            public int getCode() { return res.getStatusCode(); }

            @HostAccess.Export
            public String json() {
                return res.getBody(); // Should parse, but returning string for simple mock
            }
        }
    }
}

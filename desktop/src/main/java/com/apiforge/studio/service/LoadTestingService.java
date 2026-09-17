package com.apiforge.studio.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LoadTestingService {

    public static class LoadTestResult {
        public int totalRequests;
        public int successCount;
        public int failureCount;
        public long minLatencyMs;
        public long maxLatencyMs;
        public double avgLatencyMs;
        public long p50Ms;
        public long p90Ms;
        public long p99Ms;
        public double tps;
    }

    public void runLoadTest(String url, String method, String body, int concurrentUsers, int loops, Consumer<LoadTestResult> callback) {
        new Thread(() -> {
            List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
            List<Boolean> successList = Collections.synchronizedList(new ArrayList<>());
            
            long testStartTime = System.currentTimeMillis();
            ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            for (int i = 0; i < concurrentUsers; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < loops; j++) {
                        long reqStart = System.currentTimeMillis();
                        try {
                            HttpRequest.Builder builder = HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .timeout(Duration.ofSeconds(5));

                            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                                builder.method(method, HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
                                builder.header("Content-Type", "application/json");
                            } else {
                                builder.method(method, HttpRequest.BodyPublishers.noBody());
                            }

                            HttpResponse<Void> resp = client.send(builder.build(), HttpResponse.BodyHandlers.discarding());
                            long reqEnd = System.currentTimeMillis();
                            latencies.add(reqEnd - reqStart);
                            successList.add(resp.statusCode() >= 200 && resp.statusCode() < 300);
                        } catch (Exception e) {
                            long reqEnd = System.currentTimeMillis();
                            latencies.add(reqEnd - reqStart);
                            successList.add(false);
                        }
                    }
                });
            }

            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long testEndTime = System.currentTimeMillis();
            long totalTimeMs = testEndTime - testStartTime;

            // Calculations
            LoadTestResult result = new LoadTestResult();
            result.totalRequests = latencies.size();
            
            if (result.totalRequests == 0) {
                callback.accept(result);
                return;
            }

            result.successCount = (int) successList.stream().filter(b -> b).count();
            result.failureCount = result.totalRequests - result.successCount;

            List<Long> sortedLatencies = new ArrayList<>(latencies);
            Collections.sort(sortedLatencies);

            result.minLatencyMs = sortedLatencies.get(0);
            result.maxLatencyMs = sortedLatencies.get(sortedLatencies.size() - 1);
            result.avgLatencyMs = sortedLatencies.stream().mapToLong(x -> x).average().orElse(0.0);

            int p50Idx = (int) (sortedLatencies.size() * 0.50);
            int p90Idx = (int) (sortedLatencies.size() * 0.90);
            int p99Idx = (int) (sortedLatencies.size() * 0.99);

            result.p50Ms = sortedLatencies.get(Math.min(p50Idx, sortedLatencies.size() - 1));
            result.p90Ms = sortedLatencies.get(Math.min(p90Idx, sortedLatencies.size() - 1));
            result.p99Ms = sortedLatencies.get(Math.min(p99Idx, sortedLatencies.size() - 1));

            double durationSec = totalTimeMs / 1000.0;
            result.tps = durationSec > 0 ? result.totalRequests / durationSec : result.totalRequests;

            callback.accept(result);
        }).start();
    }
}

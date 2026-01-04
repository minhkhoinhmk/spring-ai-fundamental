package com.nhmk.agentic_example.infrastructure.webextraction;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import javax.net.ssl.SSLHandshakeException;

/**
 * A robust HTTP-based PageFetcher using Java 17 HttpClient.
 * - follows redirects
 * - supports timeouts
 * - reads up to a configured max bytes
 * - returns content-type and raw bytes for parsing
 */
@Slf4j
public class HttpPageFetcher implements PageFetcher {

    private final HttpClient client;
    private final int timeoutMs;
    private final String userAgent;
    private final int maxBytes;
    private final int maxRetries;

    public HttpPageFetcher(int timeoutMs, String userAgent, int maxBytes, int maxRetries) {
        this.timeoutMs = timeoutMs;
        this.userAgent = userAgent;
        this.maxBytes = maxBytes;
        this.maxRetries = maxRetries;
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    @Override
    public FetchResult fetch(String url) {
        if (url == null || url.isBlank()) return new FetchResult("empty-url");

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .header("User-Agent", userAgent)
                        .header("Accept", "*/*")
                        .timeout(Duration.ofMillis(timeoutMs))
                        .GET()
                        .build();

                HttpResponse<InputStream> resp = client.send(req, BodyHandlers.ofInputStream());
                int status = resp.statusCode();
                String ct = resp.headers().firstValue("Content-Type").orElse("");

                try (InputStream in = resp.body(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int read;
                    int total = 0;
                    while ((read = in.read(buffer)) != -1) {
                        total += read;
                        if (total > maxBytes) {
                            log.info("Truncated download for {} after {} bytes", url, maxBytes);
                            break;
                        }
                        out.write(buffer, 0, read);
                    }
                    byte[] body = out.toByteArray();
                    return new FetchResult(status, ct, body);
                }
            } catch (SSLHandshakeException ssl) {
                log.error("SSL handshake failed for {}: {}", url, ssl.getMessage());
                return new FetchResult("ssl-error");
            } catch (Exception e) {
                log.info("HttpPageFetcher attempt {} failed for {}: {}", attempt, url, e.getMessage());
                if (attempt == maxRetries) {
                    log.error("HttpPageFetcher failed after {} attempts for {}", maxRetries, url);
                    return new FetchResult("fetch-error");
                }
                try { Thread.sleep(200L * attempt); } catch (InterruptedException ignored) {}
            }
        }
        return new FetchResult("fetch-failed");
    }
}

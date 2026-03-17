package edu.eci.arem.framework.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;

    public HttpRequest(String method, String path, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = Map.copyOf(queryParams);
    }

    public static Optional<HttpRequest> parse(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank()) {
            return Optional.empty();
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            return Optional.empty();
        }

        URI uri = URI.create(parts[1]);

        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            // Skip headers for this minimal framework.
        }

        return Optional.of(new HttpRequest(
                parts[0].toUpperCase(Locale.ROOT),
                normalizePath(uri.getPath()),
                parseQuery(uri.getRawQuery())
        ));
    }

    private static String normalizePath(String value) {
        return (value == null || value.isBlank()) ? "/" : value;
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Map.of();
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (String pair : rawQuery.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            String[] pieces = pair.split("=", 2);
            String key = decode(pieces[0]);
            String value = pieces.length > 1 ? decode(pieces[1]) : "";
            params.put(key, value);
        }
        return params;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public Optional<String> queryParam(String name) {
        return Optional.ofNullable(queryParams.get(name));
    }

    public Map<String, String> queryParams() {
        return queryParams;
    }
}

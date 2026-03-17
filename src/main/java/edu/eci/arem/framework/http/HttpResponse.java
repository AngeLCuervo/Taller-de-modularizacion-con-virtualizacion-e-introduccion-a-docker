package edu.eci.arem.framework.http;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public final class HttpResponse {
    private final int statusCode;
    private final String reasonPhrase;
    private final String body;
    private final String contentType;

    private HttpResponse(int statusCode, String reasonPhrase, String body, String contentType) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.body = body;
        this.contentType = contentType;
    }

    public static HttpResponse ok(String body) {
        return new HttpResponse(200, "OK", body, "text/plain");
    }

    public static HttpResponse badRequest(String body) {
        return new HttpResponse(400, "Bad Request", body, "text/plain");
    }

    public static HttpResponse notFound(String body) {
        return new HttpResponse(404, "Not Found", body, "text/plain");
    }

    public static HttpResponse methodNotAllowed(String body) {
        return new HttpResponse(405, "Method Not Allowed", body, "text/plain");
    }

    public static HttpResponse internalServerError(String body) {
        return new HttpResponse(500, "Internal Server Error", body, "text/plain");
    }

    public byte[] toBytes() {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String headers = "HTTP/1.1 " + statusCode + " " + reasonPhrase + "\r\n"
                + "Content-Type: " + contentType + "; charset=UTF-8\r\n"
                + "Content-Length: " + bodyBytes.length + "\r\n"
                + "Connection: close\r\n\r\n";

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.writeBytes(headers.getBytes(StandardCharsets.UTF_8));
        output.writeBytes(bodyBytes);
        return output.toByteArray();
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }
}

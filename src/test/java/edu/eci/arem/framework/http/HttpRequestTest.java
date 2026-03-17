package edu.eci.arem.framework.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpRequestTest {

    @Test
    void parseShouldReadMethodPathAndQueryParams() throws Exception {
        String rawRequest = "GET /hello?name=Maria%20Paula HTTP/1.1\r\nHost: localhost\r\n\r\n";

        Optional<HttpRequest> request = HttpRequest.parse(
                new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8))
        );

        assertTrue(request.isPresent());
        assertEquals("GET", request.get().method());
        assertEquals("/hello", request.get().path());
        assertEquals("Maria Paula", request.get().queryParam("name").orElseThrow());
    }
}

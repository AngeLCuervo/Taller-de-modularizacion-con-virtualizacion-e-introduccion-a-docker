package edu.eci.arem.framework;

import edu.eci.arem.framework.http.HttpRequest;
import edu.eci.arem.framework.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MiniWebFrameworkTest {

    @Test
    void helloRouteShouldUseDefaultAndProvidedName() {
        MiniWebFramework framework = new MiniWebFramework();
        framework.get("/hello", request -> HttpResponse.ok(
                "Hello, " + request.queryParam("name").orElse("World") + "!"
        ));

        HttpResponse defaultResponse = framework.handle(new HttpRequest("GET", "/hello", Map.of()));
        HttpResponse namedResponse = framework.handle(new HttpRequest("GET", "/hello", Map.of("name", "Ana")));

        assertEquals(200, defaultResponse.statusCode());
        assertEquals("Hello, World!", defaultResponse.body());
        assertEquals(200, namedResponse.statusCode());
        assertEquals("Hello, Ana!", namedResponse.body());
    }

    @Test
    void shouldReturnMethodNotAllowedForNonGetRequests() {
        MiniWebFramework framework = new MiniWebFramework();
        HttpResponse response = framework.handle(new HttpRequest("POST", "/hello", Map.of()));

        assertEquals(405, response.statusCode());
    }
}

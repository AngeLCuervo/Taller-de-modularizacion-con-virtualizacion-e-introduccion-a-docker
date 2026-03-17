package edu.eci.arem.framework;

import edu.eci.arem.framework.http.HttpRequest;
import edu.eci.arem.framework.http.HttpResponse;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MiniWebFramework {
    private final Map<String, RouteHandler> getRoutes = new ConcurrentHashMap<>();

    public void get(String path, RouteHandler handler) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        getRoutes.put(path, handler);
    }

    public HttpResponse handle(HttpRequest request) {
        if (!"GET".equals(request.method())) {
            return HttpResponse.methodNotAllowed("Method Not Allowed");
        }

        RouteHandler handler = getRoutes.get(request.path());
        if (handler == null) {
            return HttpResponse.notFound("Not Found");
        }

        try {
            return handler.handle(request);
        } catch (RuntimeException e) {
            return HttpResponse.internalServerError("Internal Server Error");
        }
    }
}

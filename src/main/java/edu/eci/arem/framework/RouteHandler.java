package edu.eci.arem.framework;

import edu.eci.arem.framework.http.HttpRequest;
import edu.eci.arem.framework.http.HttpResponse;

@FunctionalInterface
public interface RouteHandler {
    HttpResponse handle(HttpRequest request);
}

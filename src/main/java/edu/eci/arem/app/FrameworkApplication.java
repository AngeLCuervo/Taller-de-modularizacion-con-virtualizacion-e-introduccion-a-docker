package edu.eci.arem.app;

import edu.eci.arem.framework.MiniHttpServer;
import edu.eci.arem.framework.MiniWebFramework;
import edu.eci.arem.framework.http.HttpResponse;

import java.io.IOException;

public final class FrameworkApplication {
    private FrameworkApplication() {
    }

    public static void main(String[] args) throws IOException {
        int port = envAsInt("PORT", 6000);
        int threadPoolSize = envAsInt("THREAD_POOL_SIZE", Math.max(2, Runtime.getRuntime().availableProcessors()));

        MiniWebFramework app = new MiniWebFramework();
        MiniHttpServer server = new MiniHttpServer(port, threadPoolSize, app);

        app.get("/hello", request -> {
            String name = request.queryParam("name")
                    .filter(value -> !value.isBlank())
                    .orElse("World");
            return HttpResponse.ok("Hello, " + name + "!");
        });

        app.get("/shutdown", request -> {
            Thread shutdownThread = new Thread(server::stopGracefully, "server-shutdown-thread");
            shutdownThread.start();
            return HttpResponse.ok("Server is shutting down.");
        });

        Runtime.getRuntime().addShutdownHook(new Thread(server::stopGracefully, "server-shutdown-hook"));

        System.out.printf("Starting framework app on port %d with %d worker threads.%n", port, threadPoolSize);
        server.start();
        System.out.println("Framework app stopped.");
    }

    private static int envAsInt(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

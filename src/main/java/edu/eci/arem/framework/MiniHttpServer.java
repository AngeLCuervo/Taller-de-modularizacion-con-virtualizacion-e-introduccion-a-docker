package edu.eci.arem.framework;

import edu.eci.arem.framework.http.HttpRequest;
import edu.eci.arem.framework.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MiniHttpServer {
    private final int port;
    private final MiniWebFramework framework;
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket serverSocket;

    public MiniHttpServer(int port, int threadPoolSize, MiniWebFramework framework) {
        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("threadPoolSize must be greater than zero");
        }
        this.port = port;
        this.framework = framework;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void start() throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Server is already running");
        }

        serverSocket = new ServerSocket(port);
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket));
            } catch (SocketException socketException) {
                if (running.get()) {
                    throw socketException;
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (Socket socket = clientSocket) {
            Optional<HttpRequest> request = HttpRequest.parse(socket.getInputStream());
            HttpResponse response = request.map(framework::handle)
                    .orElseGet(() -> HttpResponse.badRequest("Bad Request"));

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(response.toBytes());
            outputStream.flush();
        } catch (IOException ignored) {
            // Ignore malformed/broken client connections.
        }
    }

    public void stopGracefully() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        closeServerSocket();
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    private void closeServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
                // No-op.
            }
        }
    }
}

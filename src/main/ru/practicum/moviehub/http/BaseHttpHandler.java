package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8";

    protected static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, 0);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(json.getBytes());
        }
    }

    protected static void sendNoContent(HttpExchange ex, int status) throws java.io.IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, -1);
        try (OutputStream os = ex.getResponseBody()) {
            byte[] bytes = "[]".getBytes(StandardCharsets.UTF_8);
            os.write(bytes);
        }
    }
}
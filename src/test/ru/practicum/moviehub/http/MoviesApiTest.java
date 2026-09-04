package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080"; // !!! добавьте базовую часть URL
    private static MoviesServer server;
    private static HttpClient client;
    private static Gson gson;
    private static MoviesStore store;
    private static HttpResponse.BodyHandler<String> responseBodyHandler;

    @BeforeAll
    static void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(8080, store);
        server.start();
        gson = new Gson();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        responseBodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
    }

    @BeforeEach
    void beforeEach() throws IOException, InterruptedException {
        store.resetStore();

        HashMap<String, Object> movie1 = new HashMap<>();
        movie1.put("title", "Интерстеллар");
        movie1.put("year", "2014");
        String json1 = gson.toJson(movie1);
        HttpRequest.BodyPublisher body1 = HttpRequest.BodyPublishers.ofString(json1);
        HttpRequest postReq1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(body1)
                .build();
        client.send(postReq1, responseBodyHandler);


        HashMap<String, Object> movie2 = new HashMap<>();
        movie2.put("title", "Побег из Шоушенка");
        movie2.put("year", "1994");
        String json2 = gson.toJson(movie2);
        HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest postReq2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(body2)
                .build();
        client.send(postReq2, responseBodyHandler);
    }

    @AfterAll
    static void afterAll() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");
    }

    @Test
    void addMovies() throws Exception {
        HashMap<String, Object> movie = new HashMap<>();
        movie.put("title", "Интерстеллар");
        movie.put("year", "2014");

        String json = gson.toJson(movie);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .POST(body)
                .build();

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");
    }

    @Test
    void getMovies() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, responseBodyHandler);
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String body = resp.body().trim();
        ArrayList<Movie> movies = gson.fromJson(body, new TypeToken<ArrayList<Movie>>(){}.getType());

        assertEquals(2, movies.size());

        assertEquals("Интерстеллар", movies.get(0).getTitle());
        assertEquals(2014, movies.get(0).getYear());

        assertEquals("Побег из Шоушенка", movies.get(1).getTitle());
        assertEquals(1994, movies.get(1).getYear());
    }

    @Test
    void searchMovies_byId() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(200, resp.statusCode(), "GET /movies/1 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        TreeMap<String, String> movie = gson.fromJson(body, new TypeToken<TreeMap<String, String>>(){}.getType());
        assertEquals("Интерстеллар", movie.get("title"));
        assertEquals("2014", movie.get("year"));
    }

    @Test
    void deleteMovies() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .DELETE()
                .build();

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(204, resp.statusCode(), "DELETE /movies/1 должен вернуть 204");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        HttpRequest reqGet = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();
        HttpResponse<String> respGet = client.send(reqGet, responseBodyHandler);

        String body = respGet.body().trim();
        ArrayList<Movie> movies = gson.fromJson(body, new TypeToken<ArrayList<Movie>>(){}.getType());

        assertEquals(1, movies.size());
        assertEquals("Побег из Шоушенка", movies.get(0).getTitle());
        assertEquals(1994, movies.get(0).getYear());
    }
}
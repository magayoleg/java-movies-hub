package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.*;

public class MoviesHandler implements HttpHandler {
    private MoviesStore moviesStore;

    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET":
                    handleGet(ex, path);
                    break;
                case "POST":
                    handlePost(ex);
                    break;
                case "DELETE":
                    handleDelete(ex, path);
                    break;
                default:
                    BaseHttpHandler.sendJson(ex,405, "Нет такого метода");
            }
        } catch (Exception e) {
            BaseHttpHandler.sendJson(ex,500, "Ошибка сервера");
        }
    }

    private void handleGet(HttpExchange ex, String path) throws IOException {
        try {
            Integer id = extractIdFromPath(path);
            String method = ex.getRequestMethod();
            String queryString = ex.getRequestURI().getQuery();
            Gson gson = new Gson();
            ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

            if (id != null) {
                Movie movie = moviesStore.searchMovieById(id);
                if (movie == null) {
                    BaseHttpHandler.sendJson(ex,404, "Фильм не найден");
                    return;
                }
                String json = gson.toJson(movie);
                BaseHttpHandler.sendJson(ex,200, json);
                return;
            }

            if (method.equalsIgnoreCase("GET") && queryString == null) {
                ArrayList<Movie> movies = moviesStore.getMovies();

                String json = gson.toJson(movies);
                BaseHttpHandler.sendJson(ex,200, json);
            } else if (method.equalsIgnoreCase("GET")) {
                try {
                    Optional<Integer> queryYear = Arrays.stream(queryString.split("&"))
                            .map(param -> param.split("=", 2))
                            .filter(pair -> pair.length == 2 && pair[0].equals("year"))
                            .map(pair -> pair[1].isEmpty() ? 0 : Integer.parseInt(pair[1]))
                            .findFirst();
                    ArrayList<Movie> findMovie = moviesStore.filterMovieByYear(queryYear.get());
                    String json = gson.toJson(findMovie);

                    BaseHttpHandler.sendJson(ex, 200, json);
                } catch (NumberFormatException e) {
                    BaseHttpHandler.sendJson(ex,400, "Некорректный параметр запроса — 'year'");
                }
            }
        } catch (NumberFormatException e) {
            BaseHttpHandler.sendJson(ex, 400, "Некорректный ID");
        } catch (Exception e) {
            BaseHttpHandler.sendJson(ex, 500, "Внутренняя ошибка сервера");
        }
    }

    private void handlePost(HttpExchange ex) throws IOException {
            Headers headers = ex.getRequestHeaders();
            String contentType = headers.getFirst("Content-Type");
            Gson gson = new Gson();

            if (!contentType.equals("application/json; charset=UTF-8")) {
                BaseHttpHandler.sendNoContent(ex, 415);
                return;
            }
            try {
                InputStream inputStream = ex.getRequestBody();

                String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
                HashMap<String, Object> data = gson.fromJson(json, type);

                String title = data.get("title").toString();
                String year = data.get("year").toString();

                if (year.isEmpty()) {
                    BaseHttpHandler.sendJson(ex, 422, "[год не может быть пустым]");
                    return;
                }

                int yearInt = Integer.parseInt(data.get("year").toString());

                TreeMap<Integer, Movie> store = moviesStore.getStore();
                int id = store.isEmpty() ? 1 : store.lastKey() + 1;

                boolean isValidTitle = !title.isEmpty() && title.length() <= 100;
                boolean isValidYear = yearInt >= 1888 && yearInt <= Year.now().getValue() + 1;

                if (!isValidTitle || !isValidYear) {
                    BaseHttpHandler.sendJson(ex, 422, "[\"название не должно быть пустым\", \"год должен быть между 1888 и 2026\"]");
                    return;
                }
                Movie newMovie = new Movie(id, title, yearInt);

                moviesStore.addMovie(newMovie);
                BaseHttpHandler.sendJson(ex, 201, Integer.toString(id));
            } catch (RuntimeException e) {
                BaseHttpHandler.sendJson(ex, 500, "Внутренняя ошибка сервера");
            }
    }

    private void handleDelete(HttpExchange ex, String path) throws IOException {
        Integer id = extractIdFromPath(path);
        if (id != null) {
            Movie movie = moviesStore.deleteMovie(id);
            int status = movie == null ? 404 : 204;
            BaseHttpHandler.sendNoContent(ex, status);
        }
    }

        private Integer extractIdFromPath(String path) throws NumberFormatException {
        String[] parts = path.split("/");
        if (parts.length == 3 && parts[1].equals("movies")) {
            return Integer.parseInt(parts[2]);
        }
        return null;
    }
}
package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class MoviesHandler implements HttpHandler {
    private MoviesStore moviesStore;

    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String queryString = ex.getRequestURI().getQuery();
        Gson gson = new Gson();

        if (method.equalsIgnoreCase("GET") && queryString.isEmpty()) {
            BaseHttpHandler.sendNoContent(ex);
        } else if (method.equalsIgnoreCase("GET")) {
            Optional<Integer> queryYear = Arrays.stream(queryString.split("&"))
                    .map(param -> param.split("=", 2))
                    .filter(pair -> pair.length == 2 && pair[0].equals("year"))
                    .map(pair -> pair[1].isEmpty() ? 0 : Integer.parseInt(pair[1]))
                    .findFirst();
            Movie findMovie = moviesStore.searchMovieById(queryYear.get());
            ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            String json = gson.toJson(findMovie);

            BaseHttpHandler.sendJson(ex, 200, json);
        }  else if (method.equalsIgnoreCase("POST")) {
            try {
                InputStream inputStream = ex.getRequestBody();
                String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String[] data = body.split(",");

                if(data.length < 2) {
                    BaseHttpHandler.sendJson(ex, 422, "[\"название или год не должно быть пустыми\"]");
                } else {
                    String title = data[0].split(":")[1];
                    int year = Integer.parseInt(data[1].split(":")[1]);
                    int id = moviesStore.getMovies().lastKey() + 1;

                    if(title != null && year != null && id) {

                    }

                    Movie newMovie = new Movie(id, title, year);

                    moviesStore.addMovie(newMovie);
                    BaseHttpHandler.sendJson(ex, 201, Integer.toString(id));
                }
            } catch (RuntimeException e) {
                BaseHttpHandler.sendJson(ex, 400, "[]");
            }
        }
        else {
            ex.sendResponseHeaders(405, -1);
        }
    }
}
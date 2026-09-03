package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;

public class MoviesStore {
    private TreeMap<Integer, Movie> store = new TreeMap<>();

    public MoviesStore() {

    }

    public MoviesStore(ArrayList<Movie> movieList) {
        movieList.stream().forEach(movie -> {
            store.put(movie.getId(), movie);
        });
    }

    public void addMovie(Movie movie) {
        store.put(movie.getId(), movie);
    }

    public TreeMap<Integer, Movie> getStore() {
        return store;
    }

    public ArrayList<Movie> getMovies() {
        return new ArrayList<>(store.values());
    }

    public Movie searchMovieById(Integer id) {
        return store.get(id);
    }

    public ArrayList<Movie> filterMovieByYear(Integer year) {
        ArrayList<Movie> filteredMovie = new ArrayList<>();

        store.values().forEach(movie -> {
            if (movie.getYear() == year) {
                filteredMovie.add(movie);
            }
        });
        return filteredMovie;
    }

    public Movie deleteMovie(Integer id) {
        return store.remove(id);
    }

    public void resetStore() {
        this.store = new TreeMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MoviesStore that = (MoviesStore) o;
        return Objects.equals(store, that.store);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(store);
    }
}
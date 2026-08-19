package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.Objects;
import java.util.TreeMap;

public class MoviesStore {
    private TreeMap<Integer, Movie> store = new TreeMap<>();

    public MoviesStore() {}

    public MoviesStore(TreeMap<Integer, Movie> movies) {
        this.store = movies;
    }

    public void addMovie(Movie movie) {
        store.put(movie.getId(), movie);
    }
    public TreeMap<Integer, Movie> getMovies() {
        return store;
    }
    public Movie searchMovieById(Integer id) {
        return store.get(id);
    }
    public TreeMap<Integer, Movie> filterMovieByYear(Integer year) {
        TreeMap<Integer, Movie> filteredMovie = new TreeMap<>();

        store.values().forEach(movie -> {
            if(movie.getYear() == year) {
                filteredMovie.put(movie.getId(), movie);
            }
        });
        return filteredMovie;
    }
    public void deleteMovie(Integer id) {
        store.remove(id);
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
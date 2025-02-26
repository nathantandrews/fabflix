package dev.wdal.importer;

import java.util.Iterator;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MovieManager extends AbstractManager<Movie> {
    private Map<MovieKey, Movie> movies;
    private Map<String, Movie> moviesByImportId;

	protected void init() {
        this.movies = new HashMap<>();
        this.moviesByImportId = new HashMap<>();
	}

    public MovieManager() {
    }

	protected Iterator<Movie> getIterator() {
		return movies.values().iterator();
	}

	protected int getId(Movie movie) {
        int id = 0;
        try {
            id = Integer.parseInt(movie.getId().substring(2));
        }
        catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return id;
    }

	protected Movie fromResult(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setId(rs.getString("id"));
        movie.setTitle(rs.getString("title"));
        movie.setYear(rs.getInt("year"));
        movie.setDirector(rs.getString("director"));
        movies.put(movie.getKey(), movie);
        // no import ids
        return movie;
	}

	protected String getType() {
        return "movie";
    }

	protected String toCsv(Movie movie) {
        return movie.toCsv();
    }

    public boolean hasMovieKey(MovieKey key) {
		return movies.containsKey(key);
	}
	
    public boolean hasMovieImportId(String movieImportId) {
		return moviesByImportId.containsKey(movieImportId);
	}

    public Movie getMovieByImportId(String importId) {
		return moviesByImportId.get(importId);
	}
	
	public void add(Movie movie) {
        movie.setId(String.format("tt%7d", lastId++).replace(' ', '0'));
		movies.put(movie.getKey(), movie);
		moviesByImportId.put(movie.getImportId(), movie);
        super.add(movie);
	}
}

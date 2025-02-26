package dev.wdal.importer;

import java.util.Iterator;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class GenreManager extends AbstractManager<Genre> {
    private Map<String, Genre> genresByName;

    public GenreManager() {
    }

	protected void init() {
        this.genresByName = new HashMap<>();
	}

	protected String getType() {
		return "genre";
	}

	protected Iterator<Genre> getIterator() {
		return genresByName.values().iterator();
	}

	protected String toCsv(Genre genre) {
		return genre.toCsv();
	}

	protected Genre fromResult(ResultSet rs) throws SQLException {
		Genre genre = new Genre();
		genre.setId(rs.getInt("id"));
		genre.setName(rs.getString("name"));
		genresByName.put(genre.getName(), genre);
		return genre;
	}

	protected int getId(Genre genre) {
		return genre.getId();
	}

    public boolean hasGenre(String genreName) {
		return genresByName.containsKey(genreName);
	}

    public Genre getGenreByName(String name) {
		return genresByName.get(name);
	}

    public void add(Genre genre) {
        genre.setId(lastId++);
		genresByName.put(genre.getName(), genre);
		super.add(genre);
	}
}

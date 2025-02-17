package dev.wdal.importer;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class Movie {
	private String id;
	private String importId;
	private MovieKey key;
	private float cost;
	private Map<String, Genre> genres;

	public Movie(){
		key = new MovieKey();
		genres = new HashMap<String, Genre>();
		this.cost = Math.round((5 + (Math.random() * 15)) * 100) / 100;
	}
	
	public Movie(String importId, String title, int year, String director) {
		genres = new HashMap<String, Genre>();
		this.importId = importId;
		key = new MovieKey(title, year, director);
		this.cost = Math.round((5 + (Math.random() * 15)) * 100) / 100;
	}

	public MovieKey getKey() {
		return key;
	}

	public int getYear() {
		return key.getYear();
	}

	public void setYear(int year) {
		key.setYear(year);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImportId() {
		return importId;
	}

	public void setImportId(String importId) {
		this.importId = importId;
	}

	public String getTitle() {
		return key.getTitle();
	}

	public void setTitle(String title) {
		key.setTitle(title);
	}

	public String getDirector() {
		return key.getDirector();
	}

	public void setDirector(String director) {
		key.setDirector(director);
	}

	public float getCost() {
		return cost;
	}

	public void setCost(float cost) {
		this.cost = cost;
	}

	public void addGenre(Genre genre) {
		genres.put(genre.getName(), genre);
	}

	public boolean hasGenre(String name) {
		return genres.containsKey(name);
	}

	public Iterator<Genre> getGenres() {
		return genres.values().iterator();
	}

	public String toCsv() {
		StringBuffer sb = new StringBuffer();
		sb.append("'" + getId() + "'");
		sb.append(",");
		sb.append("'" + getTitle() + "'");
		sb.append(",");
		sb.append(getYear());
		sb.append(",");
		sb.append("'" + getDirector() + "'");
		sb.append(",");
		sb.append(getCost());
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		if (getId() == null) {
			sb.append("{null}");
		}
		else if (getId().isBlank()) {
			sb.append("{blank}");
		}
		else {
			sb.append(getId());
		}
		sb.append("> [");
		if (getImportId() == null) {
			sb.append("{null}");
		}
		else if (getImportId().isBlank()) {
			sb.append("{blank}");
		}
		else {
			sb.append(getImportId());
		}
		sb.append("] ");
		sb.append(key);
		return sb.toString();
	}
}

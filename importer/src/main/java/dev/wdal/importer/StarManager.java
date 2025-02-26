package dev.wdal.importer;

import java.util.Iterator;
import java.util.Map;
import java.sql.*;
import java.util.HashMap;

public class StarManager extends AbstractManager<Star> {
    private Map<StarKey, Star> stars;
    private Map<String, Star> starsByName;

	protected void init() {
		stars = new HashMap<>();
		starsByName = new HashMap<>();
	}

	public StarManager(){
	}

	protected Iterator<Star> getIterator() {
		return stars.values().iterator();
	}

	protected int getId(Star star) {
		int id = 0;
		try {
			id = Integer.parseInt(star.getId().substring(2));
		}
		catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		return id;
	}

	protected Star fromResult(ResultSet rs) throws SQLException {
		Star star = new Star();
		star.setId(rs.getString("id"));
		star.setName(rs.getString("name"));
		star.setDob(rs.getInt("birthYear"));
		stars.put(star.getKey(), star);
		starsByName.put(star.getName(), star);
		return star;
	}

	protected String getType() {
		return "star";
	}

	protected String toCsv(Star star) {
		return star.toCsv();
	}

	public boolean hasStarKey(StarKey key) {
		return stars.containsKey(key);
	}
	
	public boolean hasStarName(String name) {
		return starsByName.containsKey(name);
	}
	
	public Star getStarByName(String name) {
		return starsByName.get(name);
	}
	
	public void add(Star star) {
		star.setId(String.format("nm%7d", lastId++).replace(' ', '0'));
		stars.put(star.getKey(), star);
		starsByName.put(star.getKey().getName(), star);
		super.add(star);
	}
}

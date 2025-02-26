package dev.wdal.importer;

public class Genre {
	private int id;
	private String name;

	public Genre(){	
	}

	public Genre(String name) {
		this.name = name;
	}

	public Genre(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toCsv() {
		StringBuffer sb = new StringBuffer();
		sb.append(getId() + ",'" + getName() + "'");
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(id);
		sb.append(">, ");
		if (name == null) {
			sb.append("{null}");
		}
		else if (name.isBlank()) {
			sb.append("{blank}");
		}
		else {
			sb.append(name);
		}
		return sb.toString();
	}
}

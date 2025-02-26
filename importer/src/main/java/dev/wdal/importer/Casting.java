package dev.wdal.importer;

public class Casting {

	private String movieImportId;
	private String starName;

	public Casting(){
	}
	
	public Casting(String movieImportId, String starName) {
		this.movieImportId = movieImportId;
		this.starName = starName;
	}

	public String getMovieImportId() {
		return movieImportId;
	}

	public void setMovieImportId(String movieImportId) {
		this.movieImportId = movieImportId;
	}

	public String getStarName() {
		return starName;
	}

	public void setStarName(String starName) {
		this.starName = starName;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		if (movieImportId == null) {
			sb.append("{null}");
		}
		else if (movieImportId.isBlank()) {
			sb.append("{blank}");
		}
		else {
			sb.append(movieImportId);
		}
		sb.append(">, <");
		if (starName == null) {
			sb.append("{null}");
		}
		else if (starName.isBlank()) {
			sb.append("{blank}");
		}
		else {
			sb.append(starName);
		}
		sb.append(">");
		return sb.toString();
	}
}

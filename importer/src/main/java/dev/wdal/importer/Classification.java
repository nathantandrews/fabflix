package dev.wdal.importer;

public class Classification {
	private String movieImportId;
	private String genreName;

	public Classification(){
	}
	
	public Classification(String movieImportId, String genreName) {
		this.movieImportId = movieImportId;
		this.genreName = genreName;
	}

	public String getMovieImportId() {
		return movieImportId;
	}

	public void setMovieImportId(String movieImportId) {
		this.movieImportId = movieImportId;
	}

	public String getGenreName() {
		return genreName;
	}

	public void setGenreName(String genreName) {
		this.genreName = genreName;
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
		if (genreName == null) {
			sb.append("{null}");
		}
		else if (genreName.isBlank()) {
			sb.append("{blank}");
		}
		else {
			sb.append(genreName);
		}
		sb.append(">");
		return sb.toString();
	}
}

package dev.wdal.importer;

public class MovieKey {
    public String title;
    public int year;
    public String director;

    public MovieKey() {
    }

    public MovieKey(String title, int year, String director) {
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDirector() {
        return this.director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getTitle() == null) {
            sb.append("{null}");
        }
        else if (getTitle().isBlank()) {
            sb.append("{blank}");
        }
        else {
            sb.append(getTitle());
        }
        sb.append(" ");
        sb.append("(" + getYear() + ")");
        sb.append(" dir by ");
        if (getTitle() == null) {
            sb.append("{null}");
        }
        else if (getTitle().isBlank()) {
            sb.append("{blank}");
        }
        else {
            sb.append(getDirector());
        }
        return sb.toString();
    }
}
package dev.wdal.importer;

public class ClassificationManager extends AbstractManager<Classification> {
    private MovieManager movieMgr;
    private GenreManager genreMgr;

    public ClassificationManager(MovieManager movieMgr, GenreManager genreMgr) {
        this.movieMgr = movieMgr;
        this.genreMgr = genreMgr;
    }

	protected String getType() {
        return "genres_in_movie";
    }

    protected String toCsv(Classification c) {
        Movie m = movieMgr.getMovieByImportId(c.getMovieImportId());
        Genre g = genreMgr.getGenreByName(c.getGenreName());
        return "'" + g.getId() + "','" + m.getId() + "'";
    }

    protected void load() {
    }
}

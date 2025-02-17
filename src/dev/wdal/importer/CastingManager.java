package dev.wdal.importer;

public class CastingManager extends AbstractManager<Casting> {
    private MovieManager movieMgr;
    private StarManager starMgr;

    public CastingManager(MovieManager movieMgr, StarManager starMgr) {
        this.movieMgr = movieMgr;
        this.starMgr = starMgr;
    }

	protected String getType() {
		return "stars_in_movie";
	}

	protected String toCsv(Casting c) {
        Movie m = movieMgr.getMovieByImportId(c.getMovieImportId());
        Star s = starMgr.getStarByName(c.getStarName());
        return "'" + s.getId() + "','" + m.getId() + "'";
    }

    protected void load() {
    }
}

package dev.wdal.importer;

public class Importer {
    private StarManager starManager;
    private StarParser starParser;
    private GenreManager genreManager;
    private MovieManager movieManager;
    private MovieParser movieParser;
    private ClassificationManager classificationManager;
    private CastingManager castingManager;
    private CastingParser castingParser;

    public Importer() {
        starManager = new StarManager();
        starParser = new StarParser(starManager);
        genreManager = new GenreManager();
        movieManager = new MovieManager();
        classificationManager = new ClassificationManager(movieManager, genreManager);
        movieParser = new MovieParser(movieManager, genreManager, classificationManager);
        castingManager = new CastingManager(movieManager, starManager);
        castingParser = new CastingParser(movieManager, starManager, castingManager);
    }

    public void run() {
        starParser.parseDocument();
        starManager.dumpCsv();
        starManager.updateDb();
        movieParser.parseDocument();
        genreManager.dumpCsv();
        genreManager.updateDb();
        movieManager.dumpCsv();
        movieManager.updateDb();
        classificationManager.dumpCsv();
        classificationManager.updateDb();
        castingParser.parseDocument();
        castingManager.dumpCsv();
        castingManager.updateDb();
        Db.close();
    }

    public static void main(String[] args) {
        Importer imp = new Importer();
        imp.run();
    }

}

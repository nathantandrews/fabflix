package dev.wdal.importer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CastingParser extends AbstractParser {
    private MovieManager movieManager;
    private StarManager starManager;
    private CastingManager castingManager;
    private String val;
    private Casting casting;

    public CastingParser(MovieManager movieManager, StarManager starManager, CastingManager castingManager) {
        this.movieManager = movieManager;
        this.starManager = starManager;
        this.castingManager = castingManager;
        this.casting = new Casting();
        openLog("stars_in_movies_errors.log");
    }

    public void parseDocument() {
        super.parseDocument("casts124.xml");
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        val = "";
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        val = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("m")) {
            if (casting.getMovieImportId() == null || casting.getMovieImportId().isBlank()) {
                return;
            }
            if (casting.getStarName() == null || casting.getStarName().isBlank()) {
                return;
            }
            if (!movieManager.hasMovieImportId(casting.getMovieImportId())) {
                errorLog.println("- casting movie import id not found, ignored: " + casting.getMovieImportId());
                errorLog.flush();
                return;
            }
            if (!starManager.hasStarName(casting.getStarName())) {
                errorLog.println("- casting star name not found, adding: " + casting.getStarName());
                errorLog.flush();
                Star newStar = new Star();
                newStar.setName(casting.getStarName());
                starManager.add(newStar);
            }
            if (castingManager.has(casting)) {
                errorLog.println("- duplicate casting found, ignored: " + casting);
                errorLog.flush();
                return;
            }
            castingManager.add(casting);
            casting = new Casting();
        }
        else if (qName.equalsIgnoreCase("a")) {
            casting.setStarName(val);
        }
        else if (qName.equalsIgnoreCase("f")) {
            casting.setMovieImportId(val);
        }
    }
}

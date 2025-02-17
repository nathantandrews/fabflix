package dev.wdal.importer;

import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Iterator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class MovieParser extends AbstractParser {
    private MovieManager movieMgr;
    private GenreManager genreMgr;
    private ClassificationManager classificationMgr;

    private String director;
    private String val;
    private Movie movie;
    private Set<String> genres;
    private GenreParser genreParser;

    public MovieParser(MovieManager movieMgr, GenreManager genreMgr, ClassificationManager classificationMgr) {
        this.movieMgr = movieMgr;
        this.genreMgr = genreMgr;
        this.classificationMgr = classificationMgr;
        this.genres = new HashSet<>();
        this.genreParser = new GenreParser();
    }

    public void parseDocument() {
        super.parseDocument("mains243.xml");
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        val = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of movie
            movie = new Movie();
            genres.clear();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        val = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("film")) {
            movie.setDirector(director);
            MovieKey key = movie.getKey();
            // check entire movie db
            if (movieMgr.hasMovieKey(key)) {
                System.out.println("- duplicate movie detected, ignored: " + key);
                return;
            }
            // only imported movies will be in here
            if (movie.getImportId() == null) {
                System.out.println("- null movie import id detected, ignored: " + key);
                return;
            }
            if (movie.getImportId().isEmpty()) {
                System.out.println("- empty movie import id detected, ignored: " + key);
                return;
            }
            if (movieMgr.hasMovieImportId(movie.getImportId())) {
                System.out.println("- duplicate movie import id detected, ignored: " + key);
                return;
            }
            if (movie.getYear() == 0) {
                System.out.println("- movie with non-number year detected, ignored: " + key);
                return;
            }
            movieMgr.add(movie);

            Iterator<String> it = genreParser.getGenres();
            while (it.hasNext()) {
                val = it.next();
                Genre genre = null;
                if (genreMgr.hasGenre(val)) {
                    genre = genreMgr.getGenreByName(val);
                }
                else {
                    genre = new Genre(val);
                    genreMgr.add(genre);
                }
                Classification classification = new Classification(movie.getImportId(), genre.getName());
                if (!classificationMgr.has(classification)) {
                    classificationMgr.add(classification);
                }
            }
        } else if (qName.equalsIgnoreCase("dirname")) {
            director = val;
        } else if (qName.equalsIgnoreCase("t")) {
            movie.setTitle(val);
        } else if (qName.equalsIgnoreCase("fid")) {
            movie.setImportId(val);
        } else if (qName.equalsIgnoreCase("cat")) {
            genreParser.clear();
            genreParser.parse(val);
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                movie.setYear(Integer.parseInt(val));
            }
            catch (NumberFormatException nfe) {
            }
        } else if (qName.equalsIgnoreCase("released")) {
            try {
                movie.setYear(Integer.parseInt(val)); // overrides year
            }
            catch (NumberFormatException nfe) {
            }
        }
    }
}

package dev.wdal.importer;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Iterator;
import java.util.Collections;

public class GenreParser extends AbstractParser {
    private static final Map<String, String> codedGenres;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("susp", "Suspense");

        m.put("romt", "Romance");
        m.put("romt.", "Romance");
        m.put("romtx", "Romance");
        m.put("ront", "Romance");
        m.put("romtadvt", "Romance");

        m.put("musc", "Music");
        m.put("muusc", "Music");

        m.put("myst", "Mystery");
        m.put("mystp", "Mystery");

        m.put("comd", "Comedy");
        m.put("comdx", "Comedy");
        m.put("cond", "Comedy");
        m.put("cmr", "Comedy");
        m.put("satire", "Satire");
        m.put("sati", "Satire");

        m.put("docu", "Documentary");
        m.put("duco", "Documentary");
        m.put("ducu", "Documentary");
        m.put("dicu", "Documentary");

        m.put("advt", "Adventure");
        m.put("adct", "Adventure");

        m.put("act", "Action");
        m.put("actn", "Action");
        m.put("axtn", "Action");
        m.put("sctn", "Action");

        m.put("west", "Western");
        m.put("west1", "Western");

        m.put("fant", "Fantasy");
        m.put("fanth", "Fantasy");
        m.put("fanth*", "Fantasy");
        m.put("epic", "Fantasy");

        m.put("scfi", "Sci-Fi");
        m.put("sxfi", "Sci-Fi");
        m.put("scif", "Sci-Fi");
        m.put("s.f.", "Sci-Fi");

        m.put("cart", "Cartoon");

        m.put("horr", "Horror");
        m.put("hor", "Horror");

        m.put("biop", "Biopic");
        m.put("biopp", "Biopic");
        m.put("biopix", "Biopic");
        m.put("biopx", "Biopic");

        m.put("hist", "History");

        m.put("drama", "Drama");
        m.put("dram", "Drama");
        m.put("draam", "Drama");
        m.put("dramn", "Drama");
        m.put("dramd", "Drama");
        m.put("dram>", "Drama");
        m.put("ram", "Drama");
        m.put("undr", "Drama");
        m.put("anti-dram", "Drama");

        m.put("surr", "Surreal");
        m.put("surl", "Surreal");
        m.put("surreal", "Surreal");

        m.put("crim", "Crime");
        m.put("cnr", "Crime");
        m.put("cnrb", "Crime");
        m.put("cnrbb", "Crime");

        m.put("noir", "Noir");

        m.put("biog", "Biography");
        m.put("bio", "Biography");
        m.put("biob", "Biography");

        m.put("disa", "Disaster");
        m.put("dist", "Disaster");

        m.put("adctx", "Adventure");

        m.put("porn", "Porn");
        m.put("porb", "Porn");
        m.put("kinky", "Kinky");

        m.put("musical", "Musical");
        m.put("muscl", "Musical");

        m.put("avant", "Avant Garde");
        m.put("avga", "Avant Garde");

        m.put("psych", "Psychological");
        m.put("psyc", "Psychological");

        m.put("homo", "Homoerotic");

        m.put("natu", "Nature");

        m.put("tv", "TV");
        m.put("tvmini", "TV");

        m.put("weird", "Weird");

        m.put("cult", "Cult");

        m.put("sports", "Sport");

        m.put("viol", "Violence");

        m.put("faml", "Family");

        m.put("allegory", "Allegory");

        m.put("verite", "Documentary");

        m.put("road", "Road");

        m.put("art", "Art");

        m.put("expm", "Experimental");

        codedGenres = Collections.unmodifiableMap(m);
    }

    private Set<String> genres;

    public GenreParser() {
        this.genres = new HashSet<>();
        openLog("genres_errors.log");
    }

    public void clear() {
        this.genres.clear();
    }

    private String decode(String codedGenre) {
        if (codedGenres.keySet().contains(codedGenre)) {
            return codedGenres.get(codedGenre);
        }
        else {
            errorLog.println("- undecodable genre found, ignored '" + codedGenre + "'");
            errorLog.flush();
            return "";
        }
    }

    public void parse(String genreSpec) {
        if (genreSpec.contains(" ")) {
            Scanner s = new Scanner(genreSpec);
            s.useDelimiter(" ");
            while (s.hasNext()) {
                String g = s.next().trim();
                g = decode(g.toLowerCase());
                if (!g.isEmpty()) {
                    genres.add(g);
                }
            }
            s.close();
        }
        else if (genreSpec.contains(".")) {
            Scanner s = new Scanner(genreSpec);
            s.useDelimiter(".");
            while (s.hasNext()) {
                String g = s.next().trim();
                g = decode(g.toLowerCase());
                if (!g.isEmpty()) {
                    genres.add(g);
                }
            }
            s.close();
        }
        else{
            String g = genreSpec.trim();
            g = decode(g.toLowerCase());
            if (!g.isEmpty()) {
                genres.add(g);
            }
        }
    }

    public Iterator<String> getGenres() {
        return genres.iterator();
    }
}

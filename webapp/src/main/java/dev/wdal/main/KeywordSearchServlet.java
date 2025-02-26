package dev.wdal.main;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Scanner;
import java.util.stream.Stream;

@WebServlet(name = "KeywordSearchServlet", urlPatterns = "/api/keyword-search")
public class KeywordSearchServlet extends AbstractMovieListServlet {
    private static final long serialVersionUID = 2L;
    private static final String SERVICE_NAME = "keyword-search";

    @Override
    protected String getServiceName()
    {
        return SERVICE_NAME;
    }

    private static final String COUNT_QUERY_START =
        "SELECT COUNT(DISTINCT m.id) AS totalMovies " +
                "FROM movies m " +
                "LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
                "LEFT JOIN genres g ON gm.genreId = g.id " +
                "LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                "LEFT JOIN stars s ON sm.starId = s.id " +
                "LEFT JOIN ratings r ON r.movieId = m.id ";
    private static final String FETCH_QUERY_START =
        "SELECT m.id, m.title, m.year, m.director, m.cost, " +
                "GROUP_CONCAT(DISTINCT CONCAT(g.name, '(', g.id, ')') ORDER BY g.name SEPARATOR ',') as genres, " +
                "GROUP_CONCAT(DISTINCT CONCAT(s.name, '(', s.id, ')') ORDER BY sc.movieCount DESC, s.name ASC SEPARATOR ',') as stars, " +
                "r.rating " +
                "FROM movies m " +
                "LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
                "LEFT JOIN genres g ON gm.genreId = g.id " +
                "LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                "LEFT JOIN stars s ON sm.starId = s.id " +
                "LEFT JOIN ratings r ON r.movieId = m.id " +
                "LEFT JOIN (SELECT sim2.starId, COUNT(sim2.movieId) AS movieCount " +
                "           FROM stars_in_movies sim2 " +
                "           GROUP BY sim2.starId) AS sc ON sc.starId = sm.starId ";

    private static final String FETCH_QUERY_GROUP_BY_CLAUSE =
        "GROUP BY m.id, m.title, m.year, m.director, r.rating";

    @Override
    protected void setConstraints(HttpServletRequest request)
    {
        clearConstraints();

        String keywords = request.getParameter("keywords");
        if (keywords != null && !keywords.isEmpty())
        {
            Scanner in = new Scanner(keywords);
            Stream<String> sin = in.tokens();
            String queryTerms = sin.collect(StringBuilder::new, (sb, element) -> {
                sb.append("+").append(element).append("* "); }, StringBuilder::append).toString().trim();
            sin.close();
            in.close();
            addConstraint("MATCH (m.title) AGAINST (? IN BOOLEAN MODE)", queryTerms);
            System.out.println(queryTerms);
        }
    }

    @Override
    protected String getCountQueryStart()
    {
        return COUNT_QUERY_START;
    }

    @Override
    protected String getFetchQueryStart()
    {
        return FETCH_QUERY_START;
    }

    @Override
    protected String getFetchQueryGroupByClause()
    {
        return FETCH_QUERY_GROUP_BY_CLAUSE;
    }
}

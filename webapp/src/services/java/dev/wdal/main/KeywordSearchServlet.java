package dev.wdal.main;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.stream.Stream;

@WebServlet(name = "KeywordSearchServlet", urlPatterns = "/api/keyword-search")
public class KeywordSearchServlet extends AbstractMovieListServlet {
    private static final long serialVersionUID = 2L;
    private static final String SERVICE_NAME = "keyword-search";
    private String keywords;
    private String queryTerms;

    @Override
    protected String getServiceName()
    {
        return SERVICE_NAME;
    }

    // here order matters
    private static final String FULL_QUERY = SearchUtils.RELEVANCE_CTE +
        "SELECT m.id, title, m.year, director, cost, relevance, " +
        "  GROUP_CONCAT(DISTINCT CONCAT(g.name, '(', g.id, ')') ORDER BY g.name SEPARATOR ',') as genres, " +
        "  GROUP_CONCAT(DISTINCT CONCAT(s.name, '(', s.id, ')') ORDER BY sc.movieCount DESC, s.name ASC SEPARATOR ',') as stars, " +
        "  r.rating " +
        "FROM movies_scored AS m " +
        "LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
        "LEFT JOIN genres g ON gm.genreId = g.id " +
        "LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
        "LEFT JOIN stars s ON sm.starId = s.id " +
        "LEFT JOIN ratings r ON r.movieId = m.id " +
        "LEFT JOIN (SELECT sim2.starId, COUNT(sim2.movieId) AS movieCount " +
        "           FROM stars_in_movies sim2 " +
        "           GROUP BY sim2.starId) AS sc ON sc.starId = sm.starId " +
        "WHERE relevance > 0 ";

    // @Override
    // protected String buildCountQueryString()
    // {
    //     return buildQueryString(COUNT_QUERY_START, true, false, false, false);
    // }

    @Override
    protected String buildFetchQueryString()
    {
        // constraints are built into the keyword-based FULL_QUERY already
        return buildQueryString(FULL_QUERY, false, true, true, true);
    }

    private double maxFtsRelevance;

    @Override
    protected void setConstraints(HttpServletRequest request)
    {
        clearConstraints();
        setConstraintJoiner("OR");

        keywords = request.getParameter("keywords");
        if (keywords != null && !keywords.isEmpty())
        {
            Scanner in = new Scanner(keywords);
            Stream<String> sin = in.tokens();
            queryTerms = sin.collect(StringBuilder::new, (sb, element) -> {
                sb.append("+").append(element).append("* "); }, StringBuilder::append).toString().trim();
            sin.close();
            in.close();
            System.out.println(queryTerms);
        }

        try
        {
            maxFtsRelevance = SearchUtils.computeMaxFtsRelevance(getConnection(), queryTerms);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            maxFtsRelevance = 1.0;
        }

        addConstraint("title=?", keywords); // exact match constraint
        addConstraint("MATCH (title) AGAINST (? IN BOOLEAN MODE)", queryTerms); // fts constraint
//        addConstraint("LOWER(title) LIKE LOWER(?)", keywords + "%"); // fuzzy a constraint
//        addConstraint("edth(LOWER(title), ?, ?)", keywords); // fuzzy b, needs threshold later
    }
//
//    protected void setCountParameters(PreparedStatement ps) throws SQLException
//    {
//        super.setCountParameters(ps);
//        ps.setInt(5, (int)(SearchUtils.ED_THRESHOLD * keywords.length())); // fuzzy b threshold
//    }
//
    protected void setFetchParameters(PreparedStatement ps) throws SQLException
    {
        SearchUtils.setFetchParameters(ps, keywords, queryTerms, SearchUtils.ED_THRESHOLD, maxFtsRelevance);
    }
}

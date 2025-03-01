package dev.wdal.main;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet(name = "AdvancedSearchServlet", urlPatterns = "/api/adv-search")
public class AdvancedSearchServlet extends AbstractMovieListServlet {
    private static final long serialVersionUID = 2L;
    private static final String SERVICE_NAME = "adv-search";

    @Override
    protected String getServiceName()
    {
        return SERVICE_NAME;
    }

    private static final String FETCH_QUERY_START =
        "SELECT m.id, title, m.year, director, cost, " +
                "GROUP_CONCAT(DISTINCT CONCAT(g.name, '(', g.id, ')') ORDER BY g.name SEPARATOR ',') as genres, " +
                "GROUP_CONCAT(DISTINCT CONCAT(s.name, '(', s.id, ')') ORDER BY sc.movieCount DESC, s.name ASC SEPARATOR ',') as stars, " +
                "r.rating, " + SearchUtils.FUZZ_A_CONTRIB_FACTOR + " AS relevance " +
                "FROM movies m " +
                "LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
                "LEFT JOIN genres g ON gm.genreId = g.id " +
                "LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                "LEFT JOIN stars s ON sm.starId = s.id " +
                "LEFT JOIN ratings r ON r.movieId = m.id " +
                "LEFT JOIN (SELECT sim2.starId, COUNT(sim2.movieId) AS movieCount " +
                "           FROM stars_in_movies sim2 " +
                "           GROUP BY sim2.starId) AS sc ON sc.starId = sm.starId";

    @Override
    protected String buildFetchQueryString()
    {
        // constraints are built into the keyword-based FULL_QUERY already
        return buildQueryString(FETCH_QUERY_START, true, true, true, true);
    }
            
    protected void addTitleConstraint(HttpServletRequest request)
    {
        String title = request.getParameter("title");
        if (title != null && !title.isEmpty())
        {
            if (title.equals("*"))
            {
                addConstraint("LOWER(title) REGEXP ?", "^[^a-zA-Z0-9]");
            }
            else
            {
                addLikeConstraint("title", title);
            }
        }
    }

    @Override
    protected void setConstraints(HttpServletRequest request)
    {
        clearConstraints();
        setConstraintJoiner("AND");

        addTitleConstraint(request);

        String year = request.getParameter("year");
        if (year != null && !year.isEmpty())
        {
            addConstraint("m.year = ?", year);
        }

        String director = request.getParameter("director");
        if (director != null && !director.isEmpty())
        {
            addLikeConstraint("director", director);
        }

        String star = request.getParameter("star");
        if (star != null && !star.isEmpty())
        {
            addLikeConstraint("s.name", star);
        }
    }
}

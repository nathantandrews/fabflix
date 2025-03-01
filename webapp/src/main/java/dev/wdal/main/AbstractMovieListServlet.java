package dev.wdal.main;

import com.google.gson.stream.JsonWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ParameterMetaData;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public abstract class AbstractMovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    private long startTime;
    private long endTime;

    private String sortBy;
    private int currentPage;
    private int moviesPerPage;
    private String orderByClause;
    private int offset;

    protected String getOrderByClause()
    {
        return orderByClause;
    }

    private String countQuery;
    private String fetchQuery;

    private List<String> conditions;
    private List<Object> parameters;
    private String constraintJoiner;

    protected void setConstraintJoiner(String constraintJoiner)
    {
        this.constraintJoiner = constraintJoiner;
    }

    private int totalMovieCount;
    private List<Movie> movies;

    protected abstract String getServiceName();

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            conditions = new ArrayList<>();
            parameters = new ArrayList<>();
            movies = new ArrayList<>();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    protected abstract void setConstraints(HttpServletRequest request);

    protected void clearConstraints()
    {
        this.conditions.clear();
        this.parameters.clear();
    }
    
    protected void addConstraint(String name, Object value)
    {
        this.conditions.add(name);
        this.parameters.add(value);
    }

    protected void addLikeConstraint(String name, String value)
    {
        addConstraint("LOWER(" + name + ") LIKE LOWER(?)", value + "%");
    }

    protected void applyConstraints(StringBuilder query)
    {
        if (!conditions.isEmpty())
        {
            String whereClause = " WHERE " + String.join(" " + constraintJoiner + " ", conditions);
            query.append(whereClause);
        }
    }

    protected static final String MOVIE_COUNT = "movieCount";
    // don't care about order here, just count with constraints
    protected static final String COUNT_QUERY_START = "SELECT COUNT(DISTINCT id) AS " + MOVIE_COUNT + " FROM movies";

    protected static final String GROUP_BY_CLAUSE = "GROUP BY m.id, title, m.year, director, cost, relevance, r.rating";

    protected String buildQueryString(String start, boolean constrain, boolean group, boolean sort, boolean limit)
    {
        StringBuilder query = new StringBuilder(start);
        if (constrain) { applyConstraints(query); }
        if (group) { query.append(" " + GROUP_BY_CLAUSE + " "); }
        if (sort) { query.append(getOrderByClause()); }
        if (limit) { query.append(" LIMIT ? OFFSET ?"); }
        query.append(";");
        return query.toString();
    }

    protected String buildCountQueryString()
    {
        return buildQueryString(COUNT_QUERY_START, true, false, false, false);
    }

    protected abstract String buildFetchQueryString();

    private void buildQueries()
    {
        countQuery = buildCountQueryString();
        System.out.println("count: " + countQuery);
        fetchQuery = buildFetchQueryString();
        System.out.println("fetch: " + fetchQuery);
    }

    private void setQueryParameters(PreparedStatement ps) throws SQLException
    {
        for (int i = 0; i < parameters.size(); i++)
        {
            ps.setObject(i + 1, parameters.get(i));
        }
    }

    protected void setCountParameters(PreparedStatement ps) throws SQLException
    {
        setQueryParameters(ps);
    }

    private void countMovies() throws IOException, SQLException
    {
        totalMovieCount = 0;
        try (PreparedStatement ps = getConnection().prepareStatement(countQuery.toString()))
        {
            setCountParameters(ps);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    totalMovieCount = rs.getInt(MOVIE_COUNT);
                }
            }
        }
        System.out.println("totalMovieCount = " + totalMovieCount);
    }

    protected void setFetchParameters(PreparedStatement ps) throws SQLException
    {
        setQueryParameters(ps);
    }

    private void getPaginatedMovies() throws IOException, SQLException
    {
        try (PreparedStatement ps = getConnection().prepareStatement(fetchQuery.toString()))
        {
            setFetchParameters(ps);
            // limit params are always there
            ParameterMetaData pm = ps.getParameterMetaData();
            ps.setInt(pm.getParameterCount() - 1, moviesPerPage);
            ps.setInt(pm.getParameterCount(), offset);
            ResultSet rs = ps.executeQuery();
            movies.clear();
            while (rs.next())
            {
                Movie movie = new Movie();
                movie.setId(rs.getString("id"));
                movie.setRating(rs.getDouble("rating"));
                movie.setTitle(rs.getString("title"));
                movie.setYear(rs.getInt("year"));
                movie.setDirector(rs.getString("director"));
                movie.setCost(rs.getDouble("cost"));
                movie.setGenres(rs.getString("genres"));
                movie.setStars(rs.getString("stars"));
                movie.setRelevance(rs.getDouble("relevance"));
                movies.add(movie);
            }
        }
    }

    private void writeMovies(HttpServletResponse response) throws IOException, SQLException
    {
        try (JsonWriter jsonWriter = new JsonWriter(response.getWriter()))
        {
            jsonWriter.beginObject();
            jsonWriter.name(MOVIE_COUNT).value(totalMovieCount);
            jsonWriter.name("movies").beginArray();
            for (Movie m : movies)
            {
                jsonWriter.beginObject();
                jsonWriter.name("id").value(m.getId());
                jsonWriter.name("rating").value(m.getRating());
                jsonWriter.name("title").value(m.getTitle());
                jsonWriter.name("year").value(m.getYear());
                jsonWriter.name("director").value(m.getDirector());
                jsonWriter.name("cost").value(m.getCost());
                jsonWriter.name("genres").value(m.getGenres());
                jsonWriter.name("stars").value(m.getStars());
                jsonWriter.name("relevance").value(m.getRelevance());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            jsonWriter.endObject();
        }
    }

    private void getDisplayConfig(HttpServletRequest request)
    {
        sortBy = request.getParameter("sortBy");
        currentPage = parseInt(request.getParameter("page"));
        moviesPerPage = parseInt(request.getParameter("moviesPerPage"));
        orderByClause = "ORDER BY ";
        switch (sortBy)
        {
            case "relevance-desc-title-asc":
                orderByClause += "relevance DESC, title ASC, m.year DESC";
                break;
            case "title-asc-rating-asc":
                orderByClause += "title ASC, r.rating ASC";
                break;
            case "title-asc-rating-desc":
                orderByClause += "title ASC, r.rating DESC";
                break;
            case "title-desc-rating-desc":
                orderByClause += "title DESC, r.rating DESC";
                break;
            case "title-desc-rating-asc":
                orderByClause += "title DESC, r.rating ASC";
                break;
            case "rating-asc-title-asc":
                orderByClause += "r.rating ASC, title ASC";
                break;
            case "rating-asc-title-desc":
                orderByClause += "r.rating ASC, title DESC";
                break;
            case "rating-desc-title-desc":
                orderByClause += "r.rating DESC, title DESC";
                break;
            case "rating-desc-title-asc":
                orderByClause += "r.rating DESC, title ASC";
                break;
            default:
                throw new IllegalArgumentException("sortBy invalid");
        }
        moviesPerPage = Math.max(1, moviesPerPage);
        offset = Math.max(0, (currentPage - 1) * moviesPerPage);
    }

    private void startTimer()
    {
        startTime = System.currentTimeMillis();
    }

    private void endTimer()
    {
        endTime = System.currentTimeMillis();
        System.out.println(getServiceName() + " request took:" + (endTime - startTime) + " ms");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        startTimer();
        response.setContentType("application/json");

        getDisplayConfig(request);

        try
        {
            setConstraints(request);
            buildQueries();
    
            try (Connection conn = getConnection())
            {
                countMovies();
                getPaginatedMovies();
                writeMovies(response);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            response.setStatus(500);
            try (JsonWriter jw = new JsonWriter(response.getWriter())) {
                jw.beginObject();
                jw.name("error").value("Internal Server Error: " + e.getMessage());
                jw.endObject();
            }
            catch (IOException ioException)
            {
                ioException.printStackTrace();
            }
        }
        endTimer();
    }
}
import com.google.gson.stream.JsonWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        long startTime = System.currentTimeMillis();

        response.setContentType("application/json");

        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String genre = request.getParameter("genre");

        String sortBy = request.getParameter("sortBy");
        int currentPage = parseInt(request.getParameter("page"));
        int moviesPerPage = parseInt(request.getParameter("moviesPerPage"));

        String orderByClause = "ORDER BY ";
        switch (sortBy)
        {
            case "title-asc-rating-asc":
                orderByClause += "m.title ASC, r.rating ASC";
                break;
            case "title-asc-rating-desc":
                orderByClause += "m.title ASC, r.rating DESC";
                break;
            case "title-desc-rating-desc":
                orderByClause += "m.title DESC, r.rating DESC";
                break;
            case "title-desc-rating-asc":
                orderByClause += "m.title DESC, r.rating ASC";
                break;
            case "rating-asc-title-asc":
                orderByClause += "r.rating ASC, m.title ASC";
                break;
            case "rating-asc-title-desc":
                orderByClause += "r.rating ASC, m.title DESC";
                break;
            case "rating-desc-title-desc":
                orderByClause += "r.rating DESC, m.title DESC";
                break;
            case "rating-desc-title-asc":
                orderByClause += "r.rating DESC, m.title ASC";
                break;
            default:
                throw new IOException("sortBy invalid");
        }

//        System.out.println("got to after params");

        moviesPerPage = Math.max(1, moviesPerPage);
        int offset = Math.max(0, (currentPage - 1) * moviesPerPage);

        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (year != null && !year.isEmpty())
        {
            conditions.add("m.year = ?");
            parameters.add(year);
        }
        if (genre != null && !genre.isEmpty())
        {
            conditions.add("gm.genreId = ?");
            parameters.add(Integer.valueOf(genre));
        }
        if (title != null && !title.isEmpty())
        {
            if (title.equals("*"))
            {
                conditions.add("LOWER(m.title) REGEXP ?");
                parameters.add("^[^a-zA-Z0-9]");
            }
            else
            {
                conditions.add("LOWER(m.title) LIKE LOWER(?) ");
                parameters.add(title + "%");
            }
        }
        if (director != null && !director.isEmpty())
        {
            conditions.add("LOWER(m.director) LIKE LOWER(?) ");
            parameters.add(director + "%");
        }
        if (star != null && !star.isEmpty())
        {
            conditions.add("LOWER(s.name) LIKE LOWER(?) ");
            parameters.add(star + "%");
        }


//        System.out.println("got to after conditions");

        StringBuilder countQuery = new StringBuilder(
                "SELECT COUNT(DISTINCT m.id) AS totalMovies " +
                        "FROM movies m " +
                        "LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
                        "LEFT JOIN genres g ON gm.genreId = g.id " +
                        "LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                        "LEFT JOIN stars s ON sm.starId = s.id " +
                        "LEFT JOIN ratings r ON r.movieId = m.id "
        );
        StringBuilder mainQuery = new StringBuilder(
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
                        "           GROUP BY sim2.starId) AS sc ON sc.starId = sm.starId "
        );


        if (!conditions.isEmpty())
        {
            String whereClause = " WHERE " + String.join(" AND ", conditions);
            countQuery.append(whereClause);
            mainQuery.append(whereClause);
        }

//        System.out.println("got to after first query build");

        countQuery.append(";");
        mainQuery.append(" GROUP BY m.id, m.title, m.year, m.director, r.rating ");
        mainQuery.append(orderByClause);
        mainQuery.append(" LIMIT ? OFFSET ?;");

//        System.out.println("got to after second query build");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement countPs = conn.prepareStatement(countQuery.toString());
             PreparedStatement mainPs = conn.prepareStatement(mainQuery.toString()))
        {
            for (int i = 0; i < parameters.size(); i++)
            {
                countPs.setObject(i + 1, parameters.get(i));
            }

//            System.out.println("got to after count query params");


//            System.out.println("CountPS: " + countPs.toString());

            ResultSet countRs = countPs.executeQuery();
//            System.out.println("got to after executing count query");
            int totalMovies = 0;
            if (countRs.next())
            {
                totalMovies = countRs.getInt("totalMovies");
            }

//            System.out.println("got to after count query results");

            for (int i = 0; i < parameters.size(); i++)
            {
                mainPs.setObject(i + 1, parameters.get(i));
            }
//            System.out.println("got to after main query params");

            mainPs.setInt(parameters.size() + 1, moviesPerPage);
            mainPs.setInt(parameters.size() + 2, offset);

//            System.out.println("MainPS: " + mainPs.toString());
            ResultSet rs = mainPs.executeQuery();

            JsonWriter jsonWriter = new JsonWriter(response.getWriter());

            jsonWriter.beginObject();
            jsonWriter.name("totalMovies").value(totalMovies);
            jsonWriter.name("movies").beginArray();
            while (rs.next())
            {
                jsonWriter.beginObject();
                jsonWriter.name("id").value(rs.getString("id"));
                jsonWriter.name("rating").value(rs.getDouble("rating"));
                jsonWriter.name("title").value(rs.getString("title"));
                jsonWriter.name("year").value(rs.getInt("year"));
                jsonWriter.name("director").value(rs.getString("director"));
                jsonWriter.name("cost").value(rs.getDouble("cost"));
                jsonWriter.name("genres").value(rs.getString("genres"));
                jsonWriter.name("stars").value(rs.getString("stars"));
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            jsonWriter.endObject();

            rs.close();
        }
        catch (Exception e)
        {
            response.setStatus(500);
            //Weird errors getting on not using the same jsonWriter or something?
            try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
                jw.beginObject();
                jw.name("error").value("Internal Server Error: " + e.getMessage());
                jw.endObject();
            }
            catch (IOException ioException)
            {
                ioException.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            System.out.println("movie-list request took:" + (endTime - startTime) + " ms");
        }
    }
}
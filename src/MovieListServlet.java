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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet
{
    private static final long serialVersionUID = 2L;
    private StringBuilder sb;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config)
    {
        try
        {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            sb = new StringBuilder();
        }
        catch (NamingException e)
        {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        long startTime = System.currentTimeMillis();
        response.setContentType("application/json"); // Response mime type
        response.setCharacterEncoding("UTF-8");
        // Retrieve parameter id from url request.
        // String id = request.getParameter("id");

        // The log message can be found in localhost log
        // request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        JsonWriter out = new JsonWriter(response.getWriter());

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection())
        {
            // Get a connection from dataSource

            String movieQuery = "SELECT m.id AS movie_id, m.title AS movie_title, " +
                    "m.year AS movie_year, m.director AS movie_director, " +
                    "r.rating AS movie_rating\n" +
                    "FROM movies m\n" +
                    "         JOIN ratings r ON m.id = r.movieId\n" +
                    "ORDER BY r.rating DESC\n" +
                    "LIMIT 20;";
            String genreQuery = "SELECT gim.movieId, GROUP_CONCAT(g.name ORDER BY g.name SEPARATOR ',') AS movie_genres\n" +
                    "FROM genres_in_movies gim\n" +
                    "         JOIN genres g ON gim.genreId = g.id\n" +
                    "         JOIN (SELECT m.id AS movie_id, r.rating\n" +
                    "               FROM movies m\n" +
                    "                        JOIN ratings r ON m.id = r.movieId\n" +
                    "               ORDER BY r.rating DESC\n" +
                    "               LIMIT 20) AS mi ON mi.movie_id = gim.movieId\n" +
                    "GROUP BY gim.movieId, mi.rating\n" +
                    "ORDER BY mi.rating DESC;";
            String starQuery = "SELECT sim.movieId, GROUP_CONCAT(s.name ORDER BY s.name SEPARATOR ',') AS movie_stars_names,\n" +
                    "       GROUP_CONCAT(s.id SEPARATOR ',') AS movie_stars_ids\n" +
                    "FROM stars_in_movies sim\n" +
                    "         JOIN stars s ON sim.starId = s.id\n" +
                    "         JOIN (SELECT m.id AS movie_id, r.rating\n" +
                    "               FROM movies m\n" +
                    "                        JOIN ratings r ON m.id = r.movieId\n" +
                    "               ORDER BY r.rating DESC\n" +
                    "               LIMIT 20) AS mi ON mi.movie_id = sim.movieId\n" +
                    "GROUP BY sim.movieId, mi.rating\n" +
                    "ORDER BY mi.rating DESC;";

            // Declare our statements
            Statement movieStatement = conn.createStatement();
            Statement genreStatement = conn.createStatement();
            Statement starStatement = conn.createStatement();

            // Perform the query
            ResultSet mrs = movieStatement.executeQuery(movieQuery);
            ResultSet grs = genreStatement.executeQuery(genreQuery);
            ResultSet srs = starStatement.executeQuery(starQuery);

            // Iterate through each row of rs
            out.beginArray();
            // Iterate through each row of rs
            while (mrs.next() && grs.next() && srs.next())
            {
                out.beginObject();
                out.name("movie_id").value(mrs.getString("movie_id"));
                out.name("movie_title").value(mrs.getString("movie_title"));
                out.name("movie_year").value(mrs.getString("movie_year"));
                out.name("movie_director").value(mrs.getString("movie_director"));
                out.name("movie_genres").value(getTopThree(grs, "movie_genres"));
                out.name("movie_stars_names").value(getTopThree(srs, "movie_stars_names"));
                out.name("movie_stars_ids").value(getTopThree(srs, "movie_stars_ids"));
                out.name("movie_rating").value(mrs.getString("movie_rating"));
                out.endObject();
            }
            out.endArray();

            mrs.close();
            grs.close();
            srs.close();
            movieStatement.close();
            genreStatement.close();
            starStatement.close();

            // Set response status to 200 (OK)
            response.setStatus(200);

        }
        catch (Exception e)
        {
            // Write error message JSON object to output
            out.beginObject();
            out.name("error").value(e.getMessage());
            out.endObject();
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally
        {
            out.close();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("movie-list request took:" + (endTime - startTime) + " ms");
        // Always remember to close db connection after usage. Here it's done by try-with-resources
    }
    private String getTopThree(ResultSet rs, String columnName) throws SQLException
    {
        sb.setLength(0);
        String[] list = rs.getString(columnName).split(",");
        for (int i = 0; i < 3 && i < list.length; i++)
        {
            if (i > 0)
            {
                sb.append(", ");
            }
            sb.append(list[i]);
        }
        return sb.toString();
    }
}

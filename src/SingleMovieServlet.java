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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet
{
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config)
    {
        try
        {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
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

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        JsonWriter out = new JsonWriter(response.getWriter());

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection())
        {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT m.id as movieId, m.title, m.year, m.director, " +
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ',') as genres, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(s.name, ' (', s.id, ')') ORDER BY s.name SEPARATOR ',') as stars, " +
                    "r.rating " +
                    "FROM movies m " +
                    "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "LEFT JOIN genres g ON gim.genreId = g.id " +
                    "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                    "LEFT JOIN stars s ON sim.starId = s.id " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "WHERE m.id = ? " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();


            out.beginArray();
            while (rs.next())
            {
                // Create a JsonObject based on the data we retrieve from rs
                out.beginObject();
                out.name("movie_id").value(rs.getString("movieId"));
                out.name("movie_title").value(rs.getString("title"));
                out.name("movie_year").value(rs.getString("year"));
                out.name("movie_director").value(rs.getString("director"));
                out.name("movie_genre").value(rs.getString("genres"));
                out.name("movie_stars").value(rs.getString("stars"));
                out.name("movie_rating").value(rs.getString("rating"));
                out.endObject();
            }
            out.endArray();

            rs.close();
            statement.close();

            // Set response status to 200 (OK)
            response.setStatus(200);

        }
        catch (Exception e)
        {
            // Write error message JSON object to output
            out.beginObject();
            out.name("errorMessage").value(e.getMessage());
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
        System.out.println("single-movie request took:" + (endTime - startTime) + " ms");

        // Always remember to close db connection after usage. Here it's done by try-with-resources
    }
}

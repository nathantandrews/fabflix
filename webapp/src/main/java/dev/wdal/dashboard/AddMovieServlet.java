package dev.wdal.dashboard;

import com.google.gson.stream.JsonWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;

// Declaring a WebServlet called AddMovieServlet, which maps to url "/api/add-movie"
@WebServlet(name = "AddMovieServlet", urlPatterns = "/_dashboard/api/add-movie")
public class AddMovieServlet extends HttpServlet
{
    private static final long serialVersionUID = 2L;
    private static final String SERVICE_NAME = "add-movie";

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    private long startTime;
    private long endTime;

    public void init(ServletConfig config)
    {
        try
        {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_rw");
        }
        catch (NamingException e)
        {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private void reportTime()
    {
        this.endTime = System.currentTimeMillis();
        System.out.println(SERVICE_NAME + " request took: " + (this.endTime - this.startTime) + " ms");
    }

    private void sendResponse(JsonWriter jw, String status, String message) throws IOException
    {
        jw.beginArray();
        jw.beginObject();
        jw.name("status").value(status);
        jw.name("message").value(message);
        jw.endObject();
        jw.endArray();
    }

    private void sendErrorResponse(JsonWriter jw, String message) throws IOException
    {
        this.sendResponse(jw, "failure", message);
    }

    private void sendSuccessResponse(JsonWriter jw, String message) throws IOException
    {
        this.sendResponse(jw, "success", message);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        this.startTime = System.currentTimeMillis();
        response.setContentType("application/json"); // Response mime type

        response.setCharacterEncoding("UTF-8");

        try (JsonWriter jw = new JsonWriter(response.getWriter()))
        {
            String movieTitle = request.getParameter("movie-title");
            String movieYear = request.getParameter("movie-year");
            String movieDirector = request.getParameter("movie-director");
            String starName = request.getParameter("star-name");
            String starDob = request.getParameter("star-dob");
            String genreName = request.getParameter("genre-name");

            boolean movieTitleExists = !movieTitle.isEmpty();
            boolean movieYearExists = !movieYear.isEmpty();
            boolean movieDirectorExists = !movieDirector.isEmpty();
            boolean starDobExists = !starDob.isEmpty();
            try
            {
                if (!movieTitleExists || !movieYearExists || !movieDirectorExists)
                {
                    throw new IllegalArgumentException("A new movie requires a title, year, and director");
                }
                if (movieYearExists && movieYear.length() != 4)
                {
                    throw new NumberFormatException("Invalid movie year format");
                }
                int movieYearInt = 0;
                try
                {
                    movieYearInt = Integer.parseInt(movieYear);
                }
                catch (NumberFormatException e)
                {
                    throw new NumberFormatException("Invalid movie year format");
                }

                int starDOBInt = 0;
                if (starDobExists)
                {
                    try
                    {
                        starDOBInt = Integer.parseInt(starDob);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new NumberFormatException("Invalid star birth year format");
                    }
                }

                try (Connection conn = dataSource.getConnection())
                {
                    String movieQuery = "SELECT id FROM movies WHERE title = ? AND year = ? AND director = ?";
                    try (PreparedStatement ps = conn.prepareStatement(movieQuery))
                    {
                        ps.setString(1, movieTitle);
                        ps.setInt(2, movieYearInt);
                        ps.setString(3, movieDirector);
                        try (ResultSet rs = ps.executeQuery())
                        {
                            if (rs.next())
                            {
                                throw new IllegalArgumentException("Movie already exists");
                            }
                        }
                    }

                    String starId = null;
                    String starQuery = "SELECT id FROM stars WHERE name = ? AND birthYear = ?";
                    try (PreparedStatement ps = conn.prepareStatement(starQuery))
                    {
                        ps.setString(1, starName);
                        ps.setInt(2, starDOBInt);
                        try (ResultSet rs = ps.executeQuery())
                        {
                            if (rs.next())
                            {
                                starId = rs.getString("id");
                            }
                        }
                    }

                    String genreId = null;
                    String genreQuery = "SELECT id FROM genres WHERE name = ?";
                    try (PreparedStatement ps = conn.prepareStatement(genreQuery))
                    {
                        ps.setString(1, genreName);
                        try (ResultSet rs = ps.executeQuery())
                        {
                            if (rs.next())
                            {
                                genreId = rs.getString("id");
                            }
                        }
                    }

                    try (CallableStatement cs = conn.prepareCall("CALL add_movie(?,?,?,?,?,?,?,?,?,?)"))
                    {
                        cs.setString("movie_title", movieTitle);
                        cs.setInt("movie_year", movieYearInt);
                        cs.setString("movie_director", movieDirector);
                        cs.setDouble("movie_cost", Math.round((5 + (Math.random() * 15)) * 100) / 100);

                        if (starId != null)
                        {
                            cs.setBoolean("new_star", false);
                            cs.setString("star_id", starId);
                            cs.setNull("star_name", java.sql.Types.VARCHAR);
                            cs.setNull("star_dob", java.sql.Types.INTEGER);
                        }
                        else
                        {
                            cs.setBoolean("new_star", true);
                            cs.setNull("star_id", java.sql.Types.VARCHAR);
                            cs.setString("star_name", starName);
                            if (starDobExists)
                            {
                                cs.setInt("star_dob", starDOBInt);
                            }
                            else
                            {
                                cs.setNull("star_dob", java.sql.Types.INTEGER);
                            }
                        }

                        cs.setBoolean("new_genre", genreId == null);
                        cs.setString("genre_name", genreName);

                        cs.execute();
                        String resultsQuery = "SELECT m.id AS movie_id, " +
                                "gim.genreId AS genre_id, " +
                                "sim.starId AS star_id " +
                                "FROM movies m " +
                                "JOIN genres_in_movies gim ON gim.movieId = m.id " +
                                "JOIN stars_in_movies sim ON sim.movieId = m.id " +
                                "WHERE m.title = ? AND m.director = ? AND m.year = ?";

                        try (PreparedStatement ps = conn.prepareStatement(resultsQuery))
                        {
                            ps.setString(1, movieTitle);
                            ps.setString(2, movieDirector);
                            ps.setInt(3, movieYearInt);
                            try (ResultSet rs = ps.executeQuery())
                            {
                                if (!rs.next())
                                {
                                    throw new RuntimeException("Error: movie not added");
                                }
                                String printedMovieId = rs.getString(1);
                                int printedGenreId = rs.getInt(2);
                                String printedStarId = rs.getString(3);
                                sendSuccessResponse(jw, "movie successfully added with movieId: " + printedMovieId + ", genreId: " + printedGenreId + ", and starId: " + printedStarId);
                            }
                        }
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                    throw new RuntimeException("Error: " + e.getMessage());
                }
            }
            catch (RuntimeException e)
            {
                sendErrorResponse(jw, e.getMessage());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        reportTime();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        doPost(request, response);
    }
}

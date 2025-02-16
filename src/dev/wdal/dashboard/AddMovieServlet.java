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
    private static final String serviceName = "add-movie";

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    private long startTime;
    private long endTime;

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

    private void reportTime()
    {
        this.endTime = System.currentTimeMillis();
        System.out.println(serviceName + " request took: " + (this.endTime - this.startTime) + " ms");
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
            if (!movieTitleExists || !movieYearExists || !movieDirectorExists)
            {
                sendErrorResponse(jw, "A new movie requires a title, year, and director");
                reportTime();
                return;
            }
            else if (movieYearExists && movieYear.length() != 4)
            {
                sendErrorResponse(jw, "Invalid movie year format");
                reportTime();
                return;
            }
            else
            {
                int movieYearInt = 0;
                try
                {
                    movieYearInt = Integer.parseInt(movieYear);
                }
                catch (NumberFormatException e)
                {
                    sendErrorResponse(jw, "Invalid movie year format");
                    reportTime();
                    return;
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
                        sendErrorResponse(jw, "Invalid star birth year format");
                        reportTime();
                        return;
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
                        ResultSet rs = ps.executeQuery();
                        if (rs.next())
                        {
                            sendErrorResponse(jw, "Movie already exists");
                            reportTime();
                            return;
                        }
                    }
                    catch (SQLException e)
                    {
                        sendErrorResponse(jw, "Error: " + e.getMessage());
                        reportTime();
                        e.printStackTrace();
                        return;
                    }

                    String starId = null;
                    String starQuery = "SELECT id FROM stars WHERE name = ? AND birthYear = ?";
                    try (PreparedStatement ps = conn.prepareStatement(starQuery))
                    {
                        ps.setString(1, starName);
                        ps.setInt(2, starDOBInt);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next())
                        {
                            starId = rs.getString("id");
                        }
                        rs.close();
                    }
                    catch (SQLException e)
                    {
                        sendErrorResponse(jw, "Error: " + e.getMessage());
                        reportTime();
                        e.printStackTrace();
                        return;
                    }

                    String genreId = null;
                    String genreQuery = "SELECT id FROM genres WHERE name = ?";
                    try (PreparedStatement ps = conn.prepareStatement(genreQuery))
                    {
                        ps.setString(1, genreName);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next())
                        {
                            genreId = rs.getString("id");
                        }
                        rs.close();
                    }
                    catch (SQLException e)
                    {
                        sendErrorResponse(jw, "Error: " + e.getMessage());
                        reportTime();
                        e.printStackTrace();
                        return;
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

                        sendSuccessResponse(jw, "movie successfully added with title: " + movieTitle + ", year: " + movieYear + ", and director: " + movieDirector);
                    }
                    catch (SQLException e)
                    {
                        sendErrorResponse(jw, "Error: " + e.getMessage());
                        reportTime();
                        e.printStackTrace();
                    }
                }
                catch (SQLException e)
                {
                    sendErrorResponse(jw, "Error: " + e.getMessage());
                    reportTime();
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        doPost(request, response);
    }
}

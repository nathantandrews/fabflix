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

// Declaring a WebServlet called AddStarServlet, which maps to url "/api/add-star"
@WebServlet(name = "AddStarServlet", urlPatterns = "/_dashboard/api/add-star")
public class AddStarServlet extends HttpServlet
{
    private static final long serialVersionUID = 2L;
    private static final String serviceName = "add-star";

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        this.startTime = System.currentTimeMillis();
        response.setContentType("application/json"); // Response mime type

        response.setCharacterEncoding("UTF-8");

        try (JsonWriter jw = new JsonWriter(response.getWriter()))
        {
            String starName = request.getParameter("star-name");
            String starBirthYear = request.getParameter("star-dob");

            boolean starNameExists = !starName.isEmpty();
            boolean starBirthYearExists = !starBirthYear.isEmpty();
            try
            {
                if (!starNameExists)
                {
                    throw new IllegalArgumentException("The star name cannot be empty");
                }
                if (starBirthYearExists && starBirthYear.length() != 4)
                {
                    throw new NumberFormatException("Invalid birth year format");
                }
                String insertStatement = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
                String starId;
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement ps = conn.prepareStatement(insertStatement))
                {
                    starId = nextStarId(conn);
                    if (starId == null)
                    {
                        throw new RuntimeException("next star id is null");
                    }
                    ps.setString(1, starId);
                    ps.setString(2, starName);

                    if (!starBirthYearExists)
                    {
                        ps.setNull(3, java.sql.Types.INTEGER);
                    }
                    else
                    {
                        try
                        {
                            int starDOBInt = Integer.parseInt(starBirthYear);
                            ps.setInt(3, starDOBInt);
                        }
                        catch (NumberFormatException e)
                        {
                            throw new NumberFormatException("Invalid birth year format");
                        }
                    }

                    ps.executeUpdate();
                    sendSuccessResponse(jw, "star successfully added with id: " + starId + " name: " + starName + ", birth year: " + starBirthYear);
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

    private String nextStarId(Connection conn) throws SQLException
    {
        String checkQuery = "SELECT s.id FROM stars s ORDER BY s.id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkQuery);
             ResultSet rs = ps.executeQuery())
        {
            if (!rs.next())
            {
                throw new SQLException("Something went wrong, no stars found in the database");
            }
            String lastIdStr = rs.getString(1);
            int lastIdNum = Integer.parseInt(lastIdStr.substring(2));
            System.out.println(lastIdNum);
            ++lastIdNum;
            String result = String.format("nm%07d", lastIdNum);
            System.out.println(result);
            return result;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}

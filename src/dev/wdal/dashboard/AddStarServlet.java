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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        long startTime = System.currentTimeMillis();
        response.setContentType("application/json"); // Response mime type

        response.setCharacterEncoding("UTF-8");

        try (JsonWriter jw = new JsonWriter(response.getWriter()))
        {
            String starName = request.getParameter("star-name");
            String starBirthYear = request.getParameter("star-dob");

            boolean starNameExists = !starName.isEmpty();
            boolean starBirthYearExists = !starBirthYear.isEmpty();
            jw.beginArray();
            if (!starNameExists)
            {
                jw.beginObject();
                jw.name("status").value("failure");
                jw.name("message").value("The star name cannot be empty");
                jw.endObject();
            }
            else if (starBirthYearExists && starBirthYear.length() != 4)
            {
                jw.beginObject();
                jw.name("status").value("failure");
                jw.name("message").value("Invalid birth year format");
                jw.endObject();
            }
            else
            {
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
                            jw.beginObject();
                            jw.name("status").value("failure");
                            jw.name("message").value("Invalid birth year format");
                            jw.endObject();
                            jw.endArray();
                            long endTime = System.currentTimeMillis();
                            System.out.println("add-star request took: " + (endTime - startTime) + " ms");
                            return;
                        }
                    }

                    ps.executeUpdate();
                    jw.beginObject();
                    jw.name("status").value("success");
                    jw.name("message").value("star successfully added with id: " + starId + " name: " + starName + ", birth year: " + starBirthYear);
                    jw.endObject();
                }
                catch (SQLException e)
                {
                    jw.beginObject();
                    jw.name("status").value("failure");
                    jw.name("message").value("Error: " + e.getMessage());
                    jw.endObject();
                    e.printStackTrace();
                }
            }
            jw.endArray();
            long endTime = System.currentTimeMillis();
            System.out.println("add-star request took: " + (endTime - startTime) + " ms");
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

    private String nextStarId(Connection conn) throws SQLException
    {
        String checkQuery = "SELECT s.id FROM stars s ORDER BY s.id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkQuery);
             ResultSet rs = ps.executeQuery())
        {
            if (rs.next())
            {
                String lastIdStr = rs.getString(1);
                int lastIdNum = Integer.parseInt(lastIdStr.substring(2));
                System.out.println(lastIdNum);
                ++lastIdNum;
                String result = String.format("nm%07d", lastIdNum);
                System.out.println(result);
                return result;
            }
            else
            {
                throw new SQLException("Something went wrong, no stars found in the database");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}

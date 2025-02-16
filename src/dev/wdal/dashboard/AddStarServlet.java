package dev.wdal.dashboard;

import com.google.gson.stream.JsonWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.*;
import java.util.Random;

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
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
                    starId = generateRandomId(conn,"s");
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
                    jw.name("message").value("star successfully added with name: " + starName + ", birth year: " + starBirthYear);
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

    private String generateRandomId(Connection conn, String type)
    {
        String typeStr;
        switch (type)
        {
            case "s":
                typeStr = "nm";
                break;
            case "m":
                typeStr = "tt";
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
        String format = typeStr + "%07d";
        Random rand = new Random();
        String newId;
        boolean exists;
        do
        {
            int randomNum = rand.nextInt(1_000_000);
            newId = String.format(format, randomNum);
            String checkQuery = "SELECT s.id FROM stars s WHERE s.id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkQuery))
            {
                ps.setString(1, newId);
                ResultSet rs = ps.executeQuery();
                exists = rs.next();
                rs.close();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
        while (exists);
        return newId;
    }
}

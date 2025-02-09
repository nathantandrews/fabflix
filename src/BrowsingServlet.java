import com.google.gson.stream.JsonWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "BrowsingServlet", urlPatterns = "/api/browsing")
public class BrowsingServlet extends HttpServlet
{
    private DataSource dataSource;

    public void init(ServletConfig config)
    {
        try
        {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        }
        catch (NamingException e)
        {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        String query = "SELECT DISTINCT g.id AS genre_id, g.name AS genre_name FROM genres g";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery();
             JsonWriter jsonWriter = new JsonWriter(response.getWriter()))
        {
            jsonWriter.beginArray();
            while (rs.next())
            {
                jsonWriter.beginObject();
                jsonWriter.name("id").value(rs.getString("genre_id"));
                jsonWriter.name("name").value(rs.getString("genre_name"));
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

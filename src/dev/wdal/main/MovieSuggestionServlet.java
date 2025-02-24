package dev.wdal.main;

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
import java.util.Scanner;
import java.util.stream.Stream;

@WebServlet(name = "MovieSuggestionServlet", urlPatterns = "/api/movie-suggestion")
public class MovieSuggestionServlet extends HttpServlet
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
        String queryString = request.getParameter("query");
        Scanner in = new Scanner(queryString);
        Stream<String> sin = in.tokens();
        String queryTerms = sin.collect(StringBuilder::new, (sb, element) -> {
            sb.append("+").append(element).append("* "); }, StringBuilder::append).toString().trim();
        sin.close();
        in.close();
        System.out.println(queryTerms);
        String idQuery = "SELECT m.id AS movie_id, m.title AS movie_title, m.year as movie_year FROM movies m WHERE MATCH (m.title) AGAINST ( '" + queryTerms + "' IN BOOLEAN MODE) LIMIT 10;";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(idQuery);
             ResultSet rs = ps.executeQuery();
             JsonWriter jsonWriter = new JsonWriter(response.getWriter()))
        {
            String title;
            jsonWriter.beginArray();
            while (rs.next())
            {
                title = rs.getString("movie_title") + " (" + rs.getString("movie_year") + ")";
                jsonWriter.beginObject();
                jsonWriter.name("value").value(title);
                jsonWriter.name("data").value(rs.getString("movie_id"));
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


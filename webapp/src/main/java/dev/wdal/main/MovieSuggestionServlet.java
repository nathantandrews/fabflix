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
        try (JsonWriter jw = new JsonWriter(response.getWriter()))
        {
            String queryString = request.getParameter("query");
            FtsUtils.search(dataSource, queryString, jw, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}


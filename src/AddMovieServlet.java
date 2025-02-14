import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;

// Declaring a WebServlet called AddMovieServlet, which maps to url "/api/add-movie"
@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet
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

        long endTime = System.currentTimeMillis();
        System.out.println("add-movie request took:" + (endTime - startTime) + " ms");
        // Always remember to close db connection after usage. Here it's done by try-with-resources
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        doPost(request, response);
    }
}

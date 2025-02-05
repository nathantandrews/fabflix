import User.User;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet
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
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        try (Connection conn = dataSource.getConnection())
        {
            long startTime = System.currentTimeMillis();
            String loginQuery = "SELECT c.id FROM Customers c WHERE c.email=? AND c.password=?";
            PreparedStatement loginStatement = conn.prepareStatement(loginQuery);

            String email = request.getParameter("email");
            String password = request.getParameter("password");

            loginStatement.setString(1, email);
            loginStatement.setString(2, password);

            ResultSet resultSet = loginStatement.executeQuery();
            JsonObject responseJsonObject = new JsonObject();

            if (resultSet.next())
            {
                // Login success:
                // set this user into the session
                request.getSession().setAttribute("user", new User(email));

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "login success");
                System.out.println("Login successful: " + email);
            }
            else
            {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid login credentials. Please check your username and password and try again");

                System.out.println("Login failed: " + email);
            }
            response.getWriter().write(responseJsonObject.toString());
            long endTime = System.currentTimeMillis();
            System.out.println("login request took:" + (endTime - startTime) + " ms");
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
            response.setStatus(500);
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        doPost(request, response);
    }
}
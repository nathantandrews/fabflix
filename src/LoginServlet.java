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
import java.util.Objects;

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

            JsonObject responseJsonObject = new JsonObject();

            String email = request.getParameter("email");
            String password = request.getParameter("password");
//            System.out.println("Email: " + email);
            if (Objects.equals(email, "") && Objects.equals(password, ""))
            {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Please input your email and password.");

                System.out.println("Login failed: null email and password");
            }
            else if (Objects.equals(email, ""))
            {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Please input your email.");

                System.out.println("Login failed: null email");
            }
            else if (Objects.equals(password, ""))
            {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Please input your password.");

                System.out.println("Login failed: null password");
            }
            else
            {
                String emailQuery = "SELECT c.id FROM customers c WHERE c.email=?";
                String passwordQuery = "SELECT c.firstName FROM customers c WHERE c.id = ? AND c.password=?";
                PreparedStatement emailStatement = conn.prepareStatement(emailQuery);
                PreparedStatement passwordStatement = conn.prepareStatement(passwordQuery);
                emailStatement.setString(1, email);
                ResultSet emailRs = emailStatement.executeQuery();
                if (emailRs.next())
                {
                    passwordStatement.setString(1, emailRs.getString("id"));
                    passwordStatement.setString(2, password);
                    ResultSet passwordRs = passwordStatement.executeQuery();
                    if (passwordRs.next())
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
                        responseJsonObject.addProperty("message", "Invalid login credentials: password");

                        System.out.println("Login failed: " + email);
                    }
                }
                else
                {
                    // Login fail
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Invalid login credentials: email");

                    System.out.println("Login failed: " + email);
                }
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
package dev.wdal.main.auth;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

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

            JsonObject jsonObj = new JsonObject();

            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
            if (gRecaptchaResponse == null || gRecaptchaResponse.isEmpty())
            {
                if (request.getSession().getAttribute("gRecaptchaResponse") == null)
                {
                    jsonObj.addProperty("status", "fail");
                    jsonObj.addProperty("message", "Recaptcha verification failed.");
                    response.getWriter().write(jsonObj.toString());
                    return;
                }
                else
                {
                    gRecaptchaResponse = (String) request.getSession().getAttribute("gRecaptchaResponse");
                }
            }
//            System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
            if (gRecaptchaResponse != null && !gRecaptchaResponse.isEmpty())
            {
                request.getSession().setAttribute("gRecaptchaResponse", gRecaptchaResponse);
                try
                {
                    RecaptchaVerifyUtils.verify(gRecaptchaResponse);
                }
                catch (Exception e)
                {
                    jsonObj.addProperty("status", "fail");
                    jsonObj.addProperty("message", "Recaptcha verification failed.");
                    response.getWriter().write(jsonObj.toString());
                    return;
                }
            }
            else
            {
                jsonObj.addProperty("status", "fail");
                jsonObj.addProperty("message", "Recaptcha verification failed.");
                response.getWriter().write(jsonObj.toString());
                return;
            }
            String email = request.getParameter("email");
            String password = request.getParameter("password");
//            System.out.println("Email: " + email);
            if (Objects.equals(email, "") && Objects.equals(password, ""))
            {
                jsonObj.addProperty("status", "fail");
                jsonObj.addProperty("message", "Please input your email and password.");

                System.out.println("Login failed: null email and password");
            }
            else if (Objects.equals(email, ""))
            {
                jsonObj.addProperty("status", "fail");
                jsonObj.addProperty("message", "Please input your email.");

                System.out.println("Login failed: null email");
            }
            else if (Objects.equals(password, ""))
            {
                jsonObj.addProperty("status", "fail");
                jsonObj.addProperty("message", "Please input your password.");

                System.out.println("Login failed: null password");
            }
            else
            {
                String emailQuery = "SELECT c.id FROM customers c WHERE c.email=?";
                String passwordQuery = "SELECT c.password AS password FROM customers c WHERE c.id = ?";
                PreparedStatement emailStatement = conn.prepareStatement(emailQuery);
                PreparedStatement passwordStatement = conn.prepareStatement(passwordQuery);
                emailStatement.setString(1, email);
                ResultSet emailRs = emailStatement.executeQuery();
                if (emailRs.next())
                {
                    passwordStatement.setString(1, emailRs.getString("id"));
                    ResultSet passwordRs = passwordStatement.executeQuery();
                    if (passwordRs.next())
                    {
                        String encryptedPassword = passwordRs.getString("password");
                        boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                        if (success)
                        {
                            // Login success:
                            // set this user into the session
                            request.getSession().setAttribute("user", new User(email));

                            jsonObj.addProperty("status", "success");
                            jsonObj.addProperty("message", "login success");
                            System.out.println("Login successful: " + email);
                        }
                        else
                        {
                            // Login fail
                            jsonObj.addProperty("status", "fail");
                            jsonObj.addProperty("message", "Invalid login credentials: password");

                            System.out.println("Login failed: " + email);
                        }
                    }
                    else
                    {
                        // Login fail
                        jsonObj.addProperty("status", "fail");
                        jsonObj.addProperty("message", "Invalid login credentials: email");

                        System.out.println("Login failed: " + email);
                    }
                }
                else
                {
                    // Login fail
                    jsonObj.addProperty("status", "fail");
                    jsonObj.addProperty("message", "Invalid login credentials: email");

                    System.out.println("Login failed: " + email);
                }
            }
            response.getWriter().write(jsonObj.toString());
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
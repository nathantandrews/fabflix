package dev.wdal.auth.main;

import dev.wdal.common.auth.User;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
//import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.AccountNotFoundException;
//import javax.security.auth.login.CredentialNotFoundException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Objects;

import static dev.wdal.common.auth.JwtUtil.generateToken;
import static dev.wdal.common.auth.JwtUtil.updateJwtCookie;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet
{

    private DataSource dataSource;

    public void init(ServletConfig config)
    {
        try
        {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_ro");
        }
        catch (NamingException e)
        {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void setFailureMessage(JsonObject jo, String message)
    {
        jo.addProperty("status", "fail");
        jo.addProperty("message", message);
    }

//    protected void doRecaptcha(HttpServletRequest request) throws LoginException
//    {
//        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
//        if (gRecaptchaResponse == null || gRecaptchaResponse.isEmpty())
//        {
//            gRecaptchaResponse = (String)request.getSession().getAttribute("gRecaptchaResponse");
//            if (gRecaptchaResponse == null || gRecaptchaResponse.isEmpty())
//            {
//                throw new CredentialNotFoundException("Recaptcha verification failed.");
//            }
//        }
//        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
//        request.getSession().setAttribute("gRecaptchaResponse", gRecaptchaResponse);
//        try
//        {
//            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//        }
//        catch (Exception e)
//        {
//            throw new FailedLoginException("Recaptcha verification failed.");
//        }
//    }

    protected void doBasicCredChecks(String email, String password) throws LoginException
    {
        //            System.out.println("Email: " + email);
        if (Objects.equals(email, "") && Objects.equals(password, ""))
        {
            System.out.println("Login failed: null/empty email and password");
            throw new FailedLoginException("Please input your email and password.");
        }
        if (Objects.equals(email, ""))
        {
            System.out.println("Login failed: null/empty email");
            throw new FailedLoginException("Please input your email.");
        }
        if (Objects.equals(password, ""))
        {
            System.out.println("Login failed: null/empty password");
            throw new FailedLoginException("Please input your password.");
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

            try
            {
//                doRecaptcha(request);
                String email = request.getParameter("email");
                String password = request.getParameter("password");
                doBasicCredChecks(email, password);
                String emailQuery = "SELECT c.id FROM customers c WHERE c.email=?";
                String passwordQuery = "SELECT c.password AS password FROM customers c WHERE c.id = ?";
                try (PreparedStatement emailStatement = conn.prepareStatement(emailQuery);
                    PreparedStatement passwordStatement = conn.prepareStatement(passwordQuery))
                {
                    emailStatement.setString(1, email);
                    try (ResultSet emailRs = emailStatement.executeQuery())
                    {
                        if (!emailRs.next())
                        {
                            System.out.println("Login failed: " + email);
                            throw new FailedLoginException("Invalid login credentials: email");
                        }
                        passwordStatement.setString(1, emailRs.getString("id"));
                        try (ResultSet passwordRs = passwordStatement.executeQuery())
                        {
                            if (!passwordRs.next())
                            {
                                throw new AccountNotFoundException("Invalid login credentials: email");
                            }
                            String encryptedPassword = passwordRs.getString("password");
                            boolean success = encryptedPassword.equals(password);
//                            boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                            if (!success)
                            {
                                System.out.println("Login failed: " + email);
                                throw new FailedLoginException("Invalid login credentials: email");
                            }
                            // Login success:
                            // set this user into the session
                            request.getSession().setAttribute("user", new User(email));
                            HashMap<String, Object> hm = new HashMap<>();
                            hm.put("email", email);
                            hm.put("userType", "customer");
                            updateJwtCookie(request, response, generateToken(emailRs.getString("id"), hm));

                            jsonObj.addProperty("status", "success");
                            jsonObj.addProperty("message", "login success");
                            System.out.println("Login successful: " + email);
                        }
                    }
                }
            }
            catch (LoginException e)
            {
                setFailureMessage(jsonObj, e.getMessage());
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
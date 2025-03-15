package dev.wdal.main.auth;

import com.google.gson.stream.JsonWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import java.io.IOException;

@WebServlet(name="LogoutServlet", urlPatterns = "/api/logout")
public class LogoutServlet extends HttpServlet
{
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        try (JsonWriter jsonWriter = new JsonWriter(response.getWriter()))
        {
            request.getSession().invalidate();
            eraseCookie(request, response, "jwtToken");
            response.setContentType("application/json");
            jsonWriter.beginObject();
            jsonWriter.name("status").value("success");
            jsonWriter.name("message").value("Logged out successfully");
            jsonWriter.endObject();
        }
        catch (Exception e)
        {
            System.out.println("Error:" + e);
            // jsonWriter.beginObject();
            // jsonWriter.name("status").value("error");
            // jsonWriter.endObject();
        }
    }
    private void eraseCookie(HttpServletRequest request, HttpServletResponse response, String cookieName)
    {
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookie.getName().equals(cookieName))
                {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }
}

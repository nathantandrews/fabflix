package dev.wdal.main.auth;

import com.google.gson.stream.JsonWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name="LogoutServlet", urlPatterns = "/api/logout")
public class LogoutServlet extends HttpServlet
{
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        try (JsonWriter jsonWriter = new JsonWriter(response.getWriter()))
        {
            request.getSession().invalidate();
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
}

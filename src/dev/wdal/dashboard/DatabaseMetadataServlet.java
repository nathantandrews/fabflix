package dev.wdal.dashboard;

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

@WebServlet(name = "DatabaseMetadataServlet", urlPatterns = "/_dashboard/api/metadata")
public class DatabaseMetadataServlet extends HttpServlet
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
        response.setContentType("application/json");
        String tableQuery = "SELECT DISTINCT TABLE_NAME\n" +
                            "FROM information_schema.columns\n" +
                            "WHERE TABLE_SCHEMA='moviedb'\n" +
                            "ORDER BY TABLE_NAME;";
        try (Connection conn = dataSource.getConnection();
             JsonWriter jw = new JsonWriter(response.getWriter()))
        {
            PreparedStatement ps = conn.prepareStatement(tableQuery);
            ResultSet rs = ps.executeQuery();
            jw.beginArray();
            while (rs.next())
            {
                String columnQuery = "SELECT COLUMN_NAME, DATA_TYPE\n" +
                        "FROM information_schema.columns\n" +
                        "WHERE TABLE_SCHEMA='moviedb' AND TABLE_NAME=?\n" +
                        "ORDER BY ORDINAL_POSITION;";
                PreparedStatement ps2 = conn.prepareStatement(columnQuery);
                ps2.setString(1, rs.getString("TABLE_NAME"));
                ResultSet rs2 = ps2.executeQuery();
                jw.beginObject();
                jw.name("tableName").value(rs.getString("TABLE_NAME"));
                jw.name("columns").beginArray();
                while (rs2.next())
                {
                    jw.beginObject();
                    jw.name("columnName").value(rs2.getString("COLUMN_NAME"));
                    jw.name("dataType").value(rs2.getString("DATA_TYPE"));
                    jw.endObject();
                }
                jw.endArray();
                jw.endObject();
                rs2.close();
                ps2.close();
            }
            jw.endArray();
            rs.close();
            ps.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

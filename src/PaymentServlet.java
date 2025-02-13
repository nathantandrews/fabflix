import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
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


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream())))
        {
            while ((line = reader.readLine()) != null)
            {
                jsonBuffer.append(line);
            }
        }

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonBuffer.toString(), JsonObject.class);

        String firstName = jsonObject.get("firstName").getAsString();
        String lastName = jsonObject.get("lastName").getAsString();
        String cardNumber = jsonObject.get("cardNumber").getAsString();
        String expirationDate = jsonObject.get("expirationDate").getAsString();
        System.out.println(firstName + " " + lastName + " " + cardNumber + " " + expirationDate);

        List<String> movieTitles = new ArrayList<>();
        jsonObject.getAsJsonArray("movieTitles").forEach(movie -> {
            movieTitles.add(movie.getAsString());
        });

        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            // First step is to validate the credit card information that the user put in.
            String creditCardQuery = "SELECT id FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(creditCardQuery))
            {
                pstmt.setString(1, cardNumber);
                pstmt.setString(2, firstName);
                pstmt.setString(3, lastName);
                pstmt.setDate(4, java.sql.Date.valueOf(expirationDate));
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next())
                {
                    sendErrorResponse(response, "Invalid payment details. Please check your credit card information.");
                    return;
                }

                rs.close();
            }

            //Retrieve the customer id based on the credit card they gave us
            String customerQuery = "SELECT id FROM customers WHERE ccId = ?";
            int customerId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(customerQuery)) {
                pstmt.setString(1, cardNumber);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    customerId = rs.getInt("id");
                }
                rs.close();
            }

            if (customerId == -1) {
                sendErrorResponse(response, "Customer not found. Please check your information.");
                return;
            }

            // We passed in movie titles, but we need movie IDs.
            List<String> movieIds = new ArrayList<>();
            for (String title : movieTitles) {
                String movieQuery = "SELECT id FROM movies WHERE title = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(movieQuery)) {
                    pstmt.setString(1, title);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        movieIds.add(rs.getString("id"));
                    } else {
                        sendErrorResponse(response, "Movie not found: " + title);
                        return;
                    }
                    rs.close();
                }
            }

            // Update the sales table
            String saleDate = java.time.LocalDate.now().toString();
            for (String movieId : movieIds) {
                String salesQuery = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(salesQuery)) {
                    pstmt.setInt(1, customerId);
                    pstmt.setString(2, movieId);
                    pstmt.setString(3, saleDate);
                    pstmt.executeUpdate();
                }
            }

            HttpSession session = request.getSession(); // Get session
            session.removeAttribute("cart");
            sendSuccessResponse(response, "Transaction successful.");

        } catch (SQLException e) {
            response.setStatus(500);
            response.getWriter().write("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", message);
        response.getWriter().write(jsonResponse.toString());
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("message", message);
        response.getWriter().write(jsonResponse.toString());
    }
}
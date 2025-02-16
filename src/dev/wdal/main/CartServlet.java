package dev.wdal.main;

import com.google.gson.stream.JsonWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;
    public void init(ServletConfig config)
    {
        try
        {
            dataSource = (DataSource) new InitialContext().lookup(
                    "java:comp/env/jdbc/moviedb");
        }
        catch (NamingException e)
        {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        response.setContentType("application/json");

        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");

        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        // Debugging: Print out the retrieved cart
        System.out.println("Retrieved Cart from Session: " + cart);

        JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(
                response.getOutputStream(), StandardCharsets.UTF_8));

        double totalPrice = 0;
        jsonWriter.beginObject();
        jsonWriter.name("cart").beginArray();
        for (Map.Entry<String, Integer> entry : cart.entrySet())
        {
            String movieTitle = entry.getKey();
            int quantity = entry.getValue();
            double cost = getMovieCost(movieTitle);
            totalPrice += cost * quantity;

            jsonWriter.beginObject();
            jsonWriter.name("title").value(movieTitle);
            jsonWriter.name("cost").value(cost);
            jsonWriter.name("quantity").value(quantity);
            jsonWriter.endObject();
        }
        jsonWriter.endArray();

        jsonWriter.name("totalPrice").value(totalPrice);
        jsonWriter.endObject();
        jsonWriter.close();
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("application/json");

        HttpSession session = request.getSession();
        System.out.println("Session ID: " + session.getId());

        System.out.println("Received title: " + request.getParameter("title"));
        System.out.println("Received action: " + request.getParameter("action"));
        System.out.println("Received quantity: " + request.getParameter("quantity"));


        @SuppressWarnings("unchecked")
        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        if (cart == null)
        {
            System.out.println("No cart found");
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        System.out.println("Session Attributes: " + session.getAttributeNames());
        System.out.println(cart);

        String item = request.getParameter("title");
        String action = request.getParameter("action");
        if (item != null && action != null)
        {
            switch (action)
            {
                case "add":
                    cart.put(item, cart.getOrDefault(item, 0) + 1);
                    break;
                case "update":
                    int newQuantity = Integer.parseInt(request.getParameter("quantity"));
                    if (newQuantity <= 0)
                    {
                        cart.remove(item);
                    }
                    else
                    {
                        cart.put(item, newQuantity);
                    }
                    break;
                case "remove":
                    cart.remove(item);
                    break;
            }
        }
        JsonWriter jw = new JsonWriter(response.getWriter());
        double totalCost = 0;
        jw.beginObject();
        jw.name("cart").beginArray();
        for (Map.Entry<String, Integer> entry : cart.entrySet())
        {
            String movieTitle = entry.getKey();
            int qty = entry.getValue();
            double movieCost = getMovieCost(movieTitle);

            totalCost += movieCost * qty;

            jw.beginObject();
            jw.name("title").value(movieTitle);
            jw.name("quantity").value(qty);
            jw.name("cost").value(movieCost);
            jw.endObject();
        }
        jw.endArray();
        jw.name("totalCost").value(totalCost);

        jw.name("message").value("Cart Updated");
        jw.endObject();
        session.setAttribute("cart", cart);
    }

    private double getMovieCost(String movieTitle) {
        String query = "SELECT cost FROM movies WHERE title = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, movieTitle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("cost");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error while fetching movie cost", e);
        }
        return 0.0;  // Default cost if not found
    }

}
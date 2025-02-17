package dev.wdal.importer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    private static String url = "jdbc:mysql://localhost:3306/moviedb?allowLoadLocalInfile=true";
    private static String username = "bandrews";
    private static String password = "123";
    private static Connection connection;
    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }
    public static void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

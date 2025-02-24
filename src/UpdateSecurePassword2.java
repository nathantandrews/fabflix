import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ServiceLoader;

public class UpdateSecurePassword2
{

    /*
     * 
     * This program updates your existing moviedb employees table to change the
     * plain text passwords to encrypted passwords.
     * 
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     * 
     */
    public static void main(String[] args) throws Exception
    {


        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();

        // change the customers table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering employees table schema completed, " + alterResult + " rows affected");

        // get the ID and password for each customer
        String query = "SELECT fullname, password from employees";

        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption) 
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<String> updateQueryList = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        while (rs.next())
        {
            // get the ID and plain text password from current table
            String fullname = rs.getString("fullname");
            String password = rs.getString("password");

            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            // generate the update query
            String updateQuery = String.format("UPDATE employees SET password='%s' WHERE fullname='%s';", encryptedPassword,
                    fullname);
            updateQueryList.add(updateQuery);
        }
        rs.close();

        // execute the update queries to update the password
        System.out.println("updating password");
        int count = 0;
        for (String updateQuery : updateQueryList)
        {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");

        statement.close();
        connection.close();
        driverShutdown();
        System.out.println("finished");

    }
    public static void driverShutdown() {
        final ClassLoader cl = ClassLoader.getSystemClassLoader();
        final Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            final Driver driver = drivers.nextElement();
            // We deregister only the classes loaded by this application's classloader
            if (driver.getClass().getClassLoader() == cl) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    System.out.println("JDBC Driver deregistration failure.");
                }
            }
        }
    }

    /**
     * Registers the JDBC drivers distributed with the application.
     */
    public static void driverInit()
    {
        Iterator<Driver> driversIterator = ServiceLoader.load(Driver.class).iterator();
        while (driversIterator.hasNext()) {
            try {
                // Instantiates the driver
                driversIterator.next();
            } catch (Throwable t) {
                System.out.println("JDBC Driver registration failure.");
            }
        }
    }

}

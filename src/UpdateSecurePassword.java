import java.sql.*;
import java.util.ArrayList;
import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class UpdateSecurePassword {

    /*
     * 
     * This program updates your existing moviedb customers table to change the
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
        String alterQuery = "ALTER TABLE customers MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering customers table schema completed, " + alterResult + " rows affected");

        // get the ID and password for each customer
        String query = "SELECT id, password from customers";

        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption) 
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<String> updateQueryList = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        while (rs.next())
        {
            // get the ID and plain text password from current table
            String id = rs.getString("id");
            String password = rs.getString("password");

            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            // generate the update query
            String updateQuery = String.format("UPDATE customers SET password='%s' WHERE id=%s;", encryptedPassword,
                    id);
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
        System.out.println("finished 1");

        Statement statement2 = connection.createStatement();

        // change the customers table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery2 = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
        int alterResult2 = statement2.executeUpdate(alterQuery2);
        System.out.println("altering employees table schema completed, " + alterResult2 + " rows affected");

        // get the ID and password for each customer
        String query2 = "SELECT fullname, password from employees";

        ResultSet rs2 = statement2.executeQuery(query2);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor2 = new StrongPasswordEncryptor();

        ArrayList<String> updateQueryList2 = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        while (rs2.next())
        {
            // get the ID and plain text password from current table
            String fullname = rs2.getString("fullname");
            String password = rs2.getString("password");

            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor2.encryptPassword(password);

            // generate the update query
            String updateQuery = String.format("UPDATE employees SET password='%s' WHERE fullname='%s';", encryptedPassword,
                    fullname);
            updateQueryList2.add(updateQuery);
        }
        rs2.close();

        // execute the update queries to update the password
        System.out.println("updating password");
        int count2 = 0;
        for (String updateQuery : updateQueryList2)
        {
            int updateResult = statement2.executeUpdate(updateQuery);
            count2 += updateResult;
        }
        System.out.println("updating password completed, " + count2 + " rows affected");

        statement2.close();
        connection.close();
        System.out.println("finished 2");

    }
}

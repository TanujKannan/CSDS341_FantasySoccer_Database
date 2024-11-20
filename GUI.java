import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class GUI {
    public static void main(String[] args) {
        // Connection URL with parameters as per the document
        String connectionUrl = "jdbc:sqlserver://cxp-sql-03\\txk498;"  // Adjust server name as needed
                             + "database=FantasySoccerDB;"                  // Change database name if necessary
                             + "user=dbuser;"
                             + "password=csds341143sdsc;"               // Replace with actual password
                             + "encrypt=true;"
                             + "trustServerCertificate=true;"
                             + "loginTimeout=15;";


        try (Connection connection = DriverManager.getConnection(connectionUrl)) {      
            System.out.println(connection); 
        }

        catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}

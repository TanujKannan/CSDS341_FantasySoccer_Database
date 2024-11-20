import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
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
            System.out.println("Connected to the database successfully!");

            Scanner scanner = new Scanner(System.in);

            // Prompt user for inputs
            System.out.print("Enter operation (INSERT, DELETE, EXCHANGE): ");
            String operation = scanner.nextLine().toUpperCase();

            System.out.print("Enter user email: ");
            String email = scanner.nextLine();

            System.out.print("Enter player first name: ");
            String fName = scanner.nextLine();

            System.out.print("Enter player last name: ");
            String lName = scanner.nextLine();

            System.out.print("Enter player's team name: ");
            String teamName = scanner.nextLine();

            String newFName = null;
            String newLName = null;
            String newTeamName = null;

            if (operation.equals("EXCHANGE")) {
                System.out.print("Enter new player first name: ");
                newFName = scanner.nextLine();

                System.out.print("Enter new player last name: ");
                newLName = scanner.nextLine();

                System.out.print("Enter new player's team name: ");
                newTeamName = scanner.nextLine();
            }

            // Call stored procedure
            String sql = "{CALL ManageUserTeam(?, ?, ?, ?, ?, ?, ?, ?)}";

            try (CallableStatement callableStatement = connection.prepareCall(sql)) {
                // Set parameters
                callableStatement.setString(1, operation); // Operation
                callableStatement.setString(2, email);    // User email
                callableStatement.setString(3, fName);    // Player first name
                callableStatement.setString(4, lName);    // Player last name
                callableStatement.setString(5, teamName); // Player's team name
                callableStatement.setString(6, newFName); // New player first name (optional)
                callableStatement.setString(7, newLName); // New player last name (optional)
                callableStatement.setString(8, newTeamName); // New player's team name (optional)

                // Execute the procedure
                callableStatement.execute();

                // Capture and print warnings
                SQLWarning warning = callableStatement.getWarnings();
                while (warning != null) {
                    System.out.println("Message: " + warning.getMessage());
                    warning = warning.getNextWarning();
                }

                System.out.println("Operation executed successfully!");
            } catch (SQLException e) {
                System.out.println("Error executing stored procedure: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

    }
}
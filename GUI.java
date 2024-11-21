import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Scanner;

public class GUI {
    public static void main(String[] args) {
        // Connection URL
        String connectionUrl = "jdbc:sqlserver://cxp-sql-03\\txk498;"
                             + "database=FantasySoccerDB;"
                             + "user=dbuser;"
                             + "password=csds341143sdsc;"
                             + "encrypt=true;"
                             + "trustServerCertificate=true;"
                             + "loginTimeout=15;";

        try (Connection connection = DriverManager.getConnection(connectionUrl)) {
            System.out.println("Connected to the database successfully!");

            Scanner scanner = new Scanner(System.in);

            // Prompt user for inputs
            System.out.print("Enter team 1 name: ");
            String team1 = scanner.nextLine();

            System.out.print("Enter team 2 name: ");
            String team2 = scanner.nextLine();

            System.out.print("Enter team 1 goals: ");
            int team1Goals = scanner.nextInt();

            System.out.print("Enter team 2 goals: ");
            int team2Goals = scanner.nextInt();

            scanner.nextLine(); // Consume newline left-over
            System.out.println("Enter goal details as 'FirstName LastName:numGoals', separated by commas:");
            String goalDetails = scanner.nextLine();

            // Call the stored procedure
            String sql = "{CALL InsertMatchAndUpdateScores(?, ?, ?, ?, ?)}";

            try (CallableStatement callableStatement = connection.prepareCall(sql)) {
                // Set parameters
                callableStatement.setString(1, team1);
                callableStatement.setString(2, team2);
                callableStatement.setInt(3, team1Goals);
                callableStatement.setInt(4, team2Goals);
                callableStatement.setString(5, goalDetails);

                // Execute the procedure
                callableStatement.execute();

                // Capture and display warnings
                SQLWarning warning = callableStatement.getWarnings();
                while (warning != null) {
                    System.out.println("Message: " + warning.getMessage());
                    warning = warning.getNextWarning();
                }

                System.out.println("Match and fantasy scores updated successfully!");
            } catch (SQLException e) {
                // Print detailed SQL error messages
                System.out.println("Error executing stored procedure: " + e.getMessage());
                Throwable cause = e.getCause();
                if (cause != null) {
                    System.out.println("Cause: " + cause.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }
}

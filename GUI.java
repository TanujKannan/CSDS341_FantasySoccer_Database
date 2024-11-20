import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class GUI {
    // Database connection details
    private static final String CONNECTION_URL = "jdbc:sqlserver://cxp-sql-03\\txk498;"
                                                + "database=FantasySoccerDB;"
                                                + "user=dbuser;"
                                                + "password=csds341143sdsc;"
                                                + "encrypt=true;"
                                                + "trustServerCertificate=true;"
                                                + "loginTimeout=15;";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(CONNECTION_URL)) {
            System.out.println("Connected to the database!");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n=== Fantasy Soccer CLI ===");
                System.out.println("1. Calculate Fantasy Score for a User's Team");
                System.out.println("2. Insert a New User and Show Result");
                System.out.println("3. Exit");
                System.out.print("Select an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        calculateFantasyScore(connection, scanner);
                        break;
                    case 2:
                        insertAndShowUser(connection, scanner);
                        break;
                    case 3:
                        System.out.println("Exiting... Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Use Case 1: Calculate Fantasy Score for a User's Team
     */
    private static void calculateFantasyScore(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter user's email: ");
            String email = scanner.nextLine();

            String sql = "{CALL GetUserFantasyScore(?)}";
            try (CallableStatement stmt = connection.prepareCall(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("User Email: " + rs.getString("user_email"));
                        System.out.println("Total Fantasy Score: " + rs.getInt("total_fantasy_score"));
                    } else {
                        System.out.println("No data found for the provided email.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while calculating fantasy score: " + e.getMessage());
        }
    }

    /**
     * Use Case 2: Insert a New User and Show Result
     */
    private static void insertAndShowUser(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter first name: ");
            String fName = scanner.nextLine();

            System.out.print("Enter last name: ");
            String lName = scanner.nextLine();

            System.out.print("Enter email: ");
            String email = scanner.nextLine();

            String sql = "{CALL InsertAndShowUser(?, ?, ?)}";
            try (CallableStatement stmt = connection.prepareCall(sql)) {
                stmt.setString(1, fName);
                stmt.setString(2, lName);
                stmt.setString(3, email);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("New User Inserted:");
                        System.out.println("First Name: " + rs.getString("f_name"));
                        System.out.println("Last Name: " + rs.getString("l_name"));
                        System.out.println("Email: " + rs.getString("email"));
                    } else {
                        System.out.println("User insertion failed.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while inserting user: " + e.getMessage());
        }
    }
}

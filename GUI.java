import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
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
                System.out.println("1. Manage User's Fantasy Team");
                System.out.println("2. Calculate Fantasy Score for a User's Team");
                System.out.println("3. Insert a New User and Show Result");
                System.out.println("4. View Players by Team");
                System.out.println("5. View Players in Your User Team"); // New option
                System.out.println("6. Exit");

                
                System.out.print("Select an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        manageUserTeam(connection, scanner);
                        break;
                    case 2:
                        calculateFantasyScore(connection, scanner);
                        break;
                    case 3:
                        insertAndShowUser(connection, scanner);
                        break;
                    case 4:
                        viewPlayersByTeam(connection, scanner);
                        break;
                    case 5:
                        viewUserTeamPlayers(connection, scanner); // New option
                        break;
                    case 6:
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
     * Use Case 1: Manage User's Fantasy Team
     */
    private static void manageUserTeam(Connection connection, Scanner scanner) {
        try {
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
        } catch (Exception e) {
            System.out.println("Error managing user's team: " + e.getMessage());
        }
    }

    /**
     * Use Case 2: Calculate Fantasy Score for a User's Team
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
     * Use Case 3: Insert a New User and Show Result
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

    /**
 * Use Case 5: View Players by Team
 */
private static void viewPlayersByTeam(Connection connection, Scanner scanner) {
    try {
        // Prompt user for the team name
        System.out.print("Enter team name: ");
        String teamName = scanner.nextLine();

        // Prepare the SQL call to the stored procedure
        String sql = "{CALL GetPlayersByTeam(?)}";

        try (CallableStatement callableStatement = connection.prepareCall(sql)) {
            // Set the input parameter
            callableStatement.setString(1, teamName);

            // Execute the procedure
            try (ResultSet rs = callableStatement.executeQuery()) {
                // Display results
                System.out.println("\nPlayers in Team: " + teamName);
                System.out.println("---------------------------------------------");
                System.out.printf("%-20s %-10s %-10s\n", "Name", "Position", "FantasyScore");

                boolean hasResults = false;

                // Process the result set
                while (rs.next()) {
                    hasResults = true;
                    String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                    String position = rs.getString("Position");
                    int fantasyScore = rs.getInt("FantasyScore");

                    // Print each player's details
                    System.out.printf("%-20s %-10s %-10d\n", name, position, fantasyScore);
                }

                // If no players found, display message
                if (!hasResults) {
                    System.out.println("No players found for the specified team.");
                }
            }
        }
    } catch (SQLException e) {
        System.out.println("Error viewing players by team: " + e.getMessage());
    }
}


/**
 * View Players in the User's Team
 */
private static void viewUserTeamPlayers(Connection connection, Scanner scanner) {
    try {
        // Prompt user for their email
        System.out.print("Enter your email: ");
        String userEmail = scanner.nextLine();

        // Prepare the SQL call to the stored procedure
        String sql = "{CALL GetUserTeamPlayers(?)}";

        try (CallableStatement callableStatement = connection.prepareCall(sql)) {
            // Set the input parameter
            callableStatement.setString(1, userEmail);

            // Execute the procedure
            try (ResultSet rs = callableStatement.executeQuery()) {
                // Display results
                System.out.println("\nPlayers in Your Team:");
                System.out.println("---------------------------------------------");
                System.out.printf("%-20s %-10s %-10s\n", "Name", "Position", "FantasyScore");

                boolean hasResults = false;

                // Process the result set
                while (rs.next()) {
                    hasResults = true;
                    String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                    String position = rs.getString("Position");
                    int fantasyScore = rs.getInt("FantasyScore");

                    // Print each player's details
                    System.out.printf("%-20s %-10s %-10d\n", name, position, fantasyScore);
                }

                // If no players found, display message
                if (!hasResults) {
                    System.out.println("No players found in your team.");
                }
            }
        }
    } catch (SQLException e) {
        System.out.println("Error viewing user team players: " + e.getMessage());
    }
}


}

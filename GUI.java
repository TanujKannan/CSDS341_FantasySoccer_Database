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
            System.out.println("5. View Players in Your User Team");
            System.out.println("6. Insert Match and Update Fantasy Scores");
            System.out.println("7. Exit");

            System.out.print("Select an option: ");
            String input = scanner.nextLine(); // Read as a string to handle invalid input
            int choice;

            // Validate the input
            try {
                choice = Integer.parseInt(input); // Convert to integer
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 7.");
                continue; // Restart the loop
            }

            // Process the choice
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
                    viewUserTeamPlayers(connection, scanner);
                    break;
                case 6:
                    insertMatchAndUpdateScores(connection, scanner);
                    break;
                case 7:
                    System.out.println("Exiting... Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please select a number between 1 and 7.");
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

            String sql = "{CALL ManageUserTeam(?, ?, ?, ?, ?, ?, ?, ?)}";

            try (CallableStatement callableStatement = connection.prepareCall(sql)) {
                callableStatement.setString(1, operation);
                callableStatement.setString(2, email);
                callableStatement.setString(3, fName);
                callableStatement.setString(4, lName);
                callableStatement.setString(5, teamName);
                callableStatement.setString(6, newFName);
                callableStatement.setString(7, newLName);
                callableStatement.setString(8, newTeamName);

                callableStatement.execute();

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
     * Use Case 4: View Players by Team
     */
    /**
 * Use Case 4: View Players by Team
 */
private static void viewPlayersByTeam(Connection connection, Scanner scanner) {
    try {
        System.out.print("Enter team name: ");
        String teamName = scanner.nextLine();

        String sql = "{CALL GetPlayersByTeam(?)}";
        try (CallableStatement callableStatement = connection.prepareCall(sql)) {
            callableStatement.setString(1, teamName);

            try (ResultSet rs = callableStatement.executeQuery()) {
                System.out.println("\nPlayers in Team: " + teamName);
                System.out.printf("%-20s %-10s %-10s %-15s\n", 
                    "Name", "Position", "FantasyScore", "TeamAverageScore");

                boolean hasResults = false;

                while (rs.next()) {
                    hasResults = true;
                    String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                    String position = rs.getString("Position");
                    int fantasyScore = rs.getInt("FantasyScore");
                    double teamAverageScore = rs.getDouble("TeamAverageScore");
                    System.out.printf("%-20s %-10s %-10d %-15.2f\n", 
                        name, position, fantasyScore, teamAverageScore);
                }

                if (!hasResults) {
                    System.out.println("No players found for the specified team.");
                }
            }
        } catch (SQLException e) {
            // Handle the "No team exists" error from the stored procedure
            if (e.getMessage().contains("No team exists")) {
                System.out.println("Error: No team exists with the specified name.");
            } else {
                System.out.println("Error viewing players by team: " + e.getMessage());
            }
        }
    } catch (Exception e) {
        System.out.println("Error viewing players by team: " + e.getMessage());
    }
}


/**
 * Use Case 5: View Players in the User's Team
 */
private static void viewUserTeamPlayers(Connection connection, Scanner scanner) {
    try {
        System.out.print("Enter your email: ");
        String userEmail = scanner.nextLine();

        String sql = "{CALL GetUserTeamPlayers(?)}";
        try (CallableStatement callableStatement = connection.prepareCall(sql)) {
            callableStatement.setString(1, userEmail);

            try (ResultSet rs = callableStatement.executeQuery()) {
                System.out.println("\nPlayers in Your Team:");
                System.out.printf("%-20s %-10s %-10s %-15s\n", 
                    "Name", "Position", "FantasyScore", "TeamTotalScore");

                boolean hasResults = false;

                while (rs.next()) {
                    hasResults = true;
                    String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                    String position = rs.getString("Position");
                    int fantasyScore = rs.getInt("FantasyScore");
                    int teamTotalScore = rs.getInt("TeamTotalScore");
                    System.out.printf("%-20s %-10s %-10d %-15d\n", 
                        name, position, fantasyScore, teamTotalScore);
                }

                if (!hasResults) {
                    System.out.println("No players found for the specified user.");
                }
            }
        } catch (SQLException e) {
            // Handle the "No user exists" or "No players found" errors
            if (e.getMessage().contains("No user exists")) {
                System.out.println("Error: No user exists with the specified email.");
            } else if (e.getMessage().contains("No players found")) {
                System.out.println("Error: No players found for the specified user.");
            } else {
                System.out.println("Error viewing user team players: " + e.getMessage());
            }
        }
    } catch (Exception e) {
        System.out.println("Error viewing user team players: " + e.getMessage());
    }
}




    /**
     * Use Case 6: Insert Match and Update Fantasy Scores
     */
    private static void insertMatchAndUpdateScores(Connection connection, Scanner scanner) {
        try {
            // Get user input
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
                callableStatement.setString(1, team1);
                callableStatement.setString(2, team2);
                callableStatement.setInt(3, team1Goals);
                callableStatement.setInt(4, team2Goals);
                callableStatement.setString(5, goalDetails);
    
                callableStatement.execute();
    
                // Handle warnings
                SQLWarning warning = callableStatement.getWarnings();
                while (warning != null) {
                    System.out.println("Message: " + warning.getMessage());
                    warning = warning.getNextWarning();
                }
    
                System.out.println("Match and fantasy scores updated successfully!");
            } catch (SQLException e) {
                // Handle database-related errors
                System.out.println("Error executing stored procedure: " + e.getMessage());
            }
        } catch (Exception e) {
            // Handle generic input errors
            System.out.println("Error reading input: " + e.getMessage());
        }
    }
}

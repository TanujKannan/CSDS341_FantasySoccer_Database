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

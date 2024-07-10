import java.sql.*;
import java.util.Scanner;

public class VotingSystem {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/voting_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "ayush@5998";
    private static final String ADMIN_PASSWORD = "aad"; // Admin password

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);

            createTables(connection);

            while (true) {
                // Ask whether the user wants to continue or exit
                System.out.println("\nMain Menu:");
                System.out.println("1. Candidate Menu");
                System.out.println("2. Voter Menu");
                System.out.println("3. Admin Menu");
                System.out.println("4. Display Results");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");
                int mainChoice = Integer.parseInt(scanner.nextLine());

                switch (mainChoice) {
                    case 1:
                        candidateMenu(connection);
                        break;
                    case 2:
                        voterMenu(connection);
                        break;
                    case 3:
                        adminMenu(connection);
                        break;
                    case 4:
                        displayResults(connection);
                        break;
                    case 5:
                        System.out.println("Exiting program. Goodbye!");
                        // Close the Scanner
                        scanner.close();
                        connection.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void adminMenu(Connection connection) {
        System.out.print("Enter the admin password: ");
        String adminPassword = scanner.nextLine();

        if (ADMIN_PASSWORD.equals(adminPassword)) {
            while (true) {
                System.out.println("\nAdmin Menu:");
                System.out.println("1. Delete Candidate");
                System.out.println("2. Delete Voter");
                System.out.println("3. Return to Main Menu");
                System.out.print("Enter your choice: ");
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        deleteCandidate(connection);
                        break;
                    case 2:
                        deleteVoter(connection);
                        break;
                    case 3:
                        return; // Return to Main Menu
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } else {
            System.out.println("Incorrect admin password. Access denied.");
        }
    }

     private static void deleteCandidate(Connection connection) {
        System.out.print("Enter the username of the candidate to be deleted: ");
        String candidateUsername = scanner.nextLine();

        try {
            // Get candidate ID
            int candidateId = getUserId(connection, "candidates", candidateUsername);

            // Delete associated votes
            String deleteVotes = "DELETE FROM votes WHERE candidate_id = ?";
            try (PreparedStatement pstmtVotes = connection.prepareStatement(deleteVotes)) {
                pstmtVotes.setInt(1, candidateId);
                pstmtVotes.executeUpdate();
            }

            // Delete candidate
            String deleteCandidate = "DELETE FROM candidates WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteCandidate)) {
                pstmt.setString(1, candidateUsername);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Candidate deleted successfully!");
                } else {
                    System.out.println("Candidate not found. Deletion failed.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting candidate: " + e.getMessage());
        }
    }

    private static void deleteVoter(Connection connection) {
        System.out.print("Enter the username of the voter to be deleted: ");
        String voterUsername = scanner.nextLine();

        try {
            // Get voter ID
            int voterId = getUserId(connection, "voters", voterUsername);

            // Delete associated votes
            String deleteVotes = "DELETE FROM votes WHERE voter_id = ?";
            try (PreparedStatement pstmtVotes = connection.prepareStatement(deleteVotes)) {
                pstmtVotes.setInt(1, voterId);
                pstmtVotes.executeUpdate();
            }

            // Delete voter
            String deleteVoter = "DELETE FROM voters WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteVoter)) {
                pstmt.setString(1, voterUsername);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Voter deleted successfully!");
                } else {
                    System.out.println("Voter not found. Deletion failed.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting voter: " + e.getMessage());
        }
    }


    private static void candidateMenu(Connection connection) {
        while (true) {
            System.out.println("\nCandidate Menu:");
            System.out.println("1. Register as a candidate");
            System.out.println("2. View all candidates");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    registerCandidate(connection);
                    break;
                case 2:
                    viewAllCandidates(connection);
                    break;
                case 3:
                    return; // Return to Main Menu
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewAllCandidates(Connection connection) {
        System.out.println("\nAll Candidates:");
        String selectCandidates = "SELECT * FROM candidates";
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(selectCandidates)) {
            while (resultSet.next()) {
                String candidateUsername = resultSet.getString("username");
                System.out.println(candidateUsername);
            }
        } catch (SQLException e) {
            System.out.println("Error displaying candidates: " + e.getMessage());
        }
    }

    private static void registerCandidate(Connection connection) {
        System.out.print("Enter your candidate username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your candidate password: ");
        String password = scanner.nextLine();

        try {
            registerCandidate(connection, username, password);
            System.out.println("Candidate registered successfully!");
        } catch (SQLException e) {
            System.out.println("Error registering candidate: " + e.getMessage());
        }
    }

    private static void registerCandidate(Connection connection, String username, String password) throws SQLException {
        String insertCandidate = "INSERT INTO candidates (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertCandidate)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        }
    }

    private static void voterMenu(Connection connection) {
        while (true) {
            System.out.println("\nVoter Menu:");
            System.out.println("1. Register as a voter");
            System.out.println("2. View all voters");
            System.out.println("3. Vote");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    registerVoter(connection);
                    break;
                case 2:
                    viewAllVoters(connection);
                    break;
                case 3:
                    vote(connection);
                    break;
                case 4:
                    return; // Return to Main Menu
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewAllVoters(Connection connection) {
        System.out.println("\nAll Voters:");
        String selectVoters = "SELECT * FROM voters";
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(selectVoters)) {
            while (resultSet.next()) {
                String voterUsername = resultSet.getString("username");
                System.out.println(voterUsername);
            }
        } catch (SQLException e) {
            System.out.println("Error displaying voters: " + e.getMessage());
        }
    }

    private static void registerVoter(Connection connection) {
        System.out.print("Enter your voter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your voter password: ");
        String password = scanner.nextLine();

        try {
            registerVoter(connection, username, password);
            System.out.println("Voter registered successfully!");
        } catch (SQLException e) {
            System.out.println("Error registering voter: " + e.getMessage());
        }
    }

    private static void registerVoter(Connection connection, String username, String password) throws SQLException {
        String insertVoter = "INSERT INTO voters (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertVoter)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        }
    }

    private static void vote(Connection connection) {
        // Display the list of candidates for the voter to choose from
        System.out.println("\nCandidate List:");
        viewAllCandidates(connection);

        // Get the voter's username
        System.out.print("Enter your voter username: ");
        String voterUsername = scanner.nextLine();

        // Get the voter's password
        System.out.print("Enter your voter password: ");
        String voterPassword = scanner.nextLine();

        try {
            // Check if the voter credentials are valid
            if (voterLogin(connection, voterUsername, voterPassword)) {
                // Get the candidate's username whom the voter wants to vote for
                System.out.print("Enter the candidate username you want to vote for: ");
                String candidateUsername = scanner.nextLine();

                try {
                    // Cast the vote
                    vote(connection, voterUsername, candidateUsername);
                    System.out.println("Vote cast successfully!");
                } catch (SQLException e) {
                    System.out.println("Error casting vote: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid voter credentials. Voting failed.");
            }
        } catch (SQLException e) {
            System.out.println("Error during voter login: " + e.getMessage());
        }
    }

    private static boolean voterLogin(Connection connection, String username, String password) throws SQLException {
        String selectVoter = "SELECT * FROM voters WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectVoter)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next();
            }
        }
    }


    private static void vote(Connection connection, String voterUsername, String candidateUsername) throws SQLException {
        int voterId = getUserId(connection, "voters", voterUsername);
        int candidateId = getUserId(connection, "candidates", candidateUsername);

        String insertVote = "INSERT INTO votes (voter_id, candidate_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertVote)) {
            pstmt.setInt(1, voterId);
            pstmt.setInt(2, candidateId);
            pstmt.executeUpdate();
        }
    }

    private static void displayResults(Connection connection) {
        System.out.println("\nVoting Results:");
        String selectCandidates = "SELECT * FROM candidates";
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(selectCandidates)) {
            while (resultSet.next()) {
                String candidateUsername = resultSet.getString("username");
                int votes = getCandidateVotes(connection, candidateUsername);
                System.out.println(candidateUsername + ": " + votes + " votes");
            }
        } catch (SQLException e) {
            System.out.println("Error displaying results: " + e.getMessage());
        }
    }

//    private static void displayCandidates(Connection connection) {
//        System.out.println("\nCandidate List:");
//        String selectCandidates = "SELECT * FROM candidates";
//        try (Statement stmt = connection.createStatement();
//             ResultSet resultSet = stmt.executeQuery(selectCandidates)) {
//            while (resultSet.next()) {
//                String candidateUsername = resultSet.getString("username");
//                System.out.println(candidateUsername);
//            }
//        } catch (SQLException e) {
//            System.out.println("Error displaying candidates: " + e.getMessage());
//        }
//    }

    private static int getCandidateVotes(Connection connection, String candidateUsername) throws SQLException {
        int candidateId = getUserId(connection, "candidates", candidateUsername);
        String selectVotes = "SELECT COUNT(*) AS vote_count FROM votes WHERE candidate_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectVotes)) {
            pstmt.setInt(1, candidateId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("vote_count");
                }
            }
        }
        return 0;
    }

    private static int getUserId(Connection connection, String table, String username) throws SQLException {
        String selectUserId = "SELECT id FROM " + table + " WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectUserId)) {
            pstmt.setString(1, username);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }
        return -1; // Return -1 if the user ID is not found
    }

    private static void createTables(Connection connection) throws SQLException {
        String createVotersTable = "CREATE TABLE IF NOT EXISTS voters (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE," +
                "password VARCHAR(50)" +
                ")";
        String createCandidatesTable = "CREATE TABLE IF NOT EXISTS candidates (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE," +
                "password VARCHAR(50)," +
                "votes INT DEFAULT 0" +
                ")";
        String createVotesTable = "CREATE TABLE IF NOT EXISTS votes (" +
                "voter_id INT," +
                "candidate_id INT," +
                "FOREIGN KEY (voter_id) REFERENCES voters(id)," +
                "FOREIGN KEY (candidate_id) REFERENCES candidates(id)" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createVotersTable);
            stmt.executeUpdate(createCandidatesTable);
            stmt.executeUpdate(createVotesTable);
        }
    }
}

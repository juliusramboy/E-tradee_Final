package com.example.etradee;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Profile_display_employee_controller {
    @FXML
    private Button cashierButton;
    @FXML
    private Label contactLabel;
    @FXML
    private Button inventoryButton;
    @FXML
    private Button logoutButton;
    @FXML
    private ImageView profilePictureImageView;
    @FXML
    private Label roleLabel;
    @FXML
    private Button salesButton;
    @FXML
    private Label usernameLabel;

    private Connection connection;
    private String loggedInUsername;
    private Stage loginStage;

    @FXML
    void inventoryButton_handle(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/etradee/InventoryEmployee.fxml"));
            Parent cashierRoot = loader.load();

            InventoryEmployeeCtlr InventoryEmployeeCtlr = loader.getController();
            InventoryEmployeeCtlr.setLoggedInUsername(loggedInUsername);
            InventoryEmployeeCtlr.setPreviousScene(((Node) event.getSource()).getScene());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene cashierScene = new Scene(cashierRoot);
            stage.setScene(cashierScene);
            stage.setTitle("Inventory");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Show an error alert
        }
    }

    @FXML
    void salesButton_handle(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/etradee/SalesEmployee.fxml"));
            Parent salesRoot = loader.load();

            SalesEmployeeCtlr salesController = loader.getController();
            salesController.setLoggedInUsername(loggedInUsername);
            salesController.setPreviousScene(((Node) event.getSource()).getScene());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene salesScene = new Scene(salesRoot);
            stage.setScene(salesScene);
            stage.setTitle("Sales Window");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // This method will be called when returning from SalesEmployeeCtlr
    public void refreshData() {
        loadUserDatabase();
    }

    @FXML
    public void initialize() {
        // Initial database connection is removed from here
    }

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        System.out.println("DEBUG: setLoggedInUsername called with: " + loggedInUsername);
        refreshConnection();
        loadUserDatabase();
    }

    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
    }

    private void refreshConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("DEBUG: Previous database connection closed.");
            }
            connection = DatabaseCon.getConnection();
            System.out.println("DEBUG: New database connection established: " + (connection != null ? "Successful" : "Failed"));
        } catch (SQLException e) {
            System.out.println("ERROR: Failed to refresh database connection");
            e.printStackTrace();
        }
    }

    private void loadUserDatabase() {
        System.out.println("DEBUG: Entering loadUserDatabase method");
        if (loggedInUsername == null) {
            System.out.println("DEBUG: loggedInUsername is null, exiting method");
            return;
        }
        System.out.println("DEBUG: Attempting to fetch profile for user: " + loggedInUsername);

        printUserProfilePictureInfo();

        try {
            String query = "SELECT username, role, profile_picture_path, contactNum FROM users WHERE username = ?";
            System.out.println("DEBUG: Executing SQL: " + query + " with username: " + loggedInUsername);

            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, loggedInUsername);

            ResultSet resultSet = pstmt.executeQuery();
            System.out.println("DEBUG: Query executed successfully");

            if (resultSet.next()) {
                System.out.println("DEBUG: User found in database");
                String username = resultSet.getString("username");
                String role = resultSet.getString("role");
                String contactNum = resultSet.getString("contactNum");

                System.out.println("DEBUG: Retrieved data - Username: " + username + ", Role: " + role + ", Contact: " + contactNum);

                byte[] profilePictureData = resultSet.getBytes("profile_picture_path");
                System.out.println("DEBUG: Profile picture data retrieved. Null? " + (profilePictureData == null));
                if (profilePictureData != null) {
                    System.out.println("DEBUG: Profile picture data length: " + profilePictureData.length);
                    System.out.println("DEBUG: First few bytes: " + bytesToHex(profilePictureData, 10));
                }

                Image profilePicture;
                if (profilePictureData != null && profilePictureData.length > 0) {
                    try {
                        profilePicture = new Image(new ByteArrayInputStream(profilePictureData));
                        System.out.println("DEBUG: Image created successfully: " + (!profilePicture.isError()));
                        if (profilePicture.isError()) {
                            System.out.println("DEBUG: Image creation error: " + profilePicture.getException());
                        }
                    } catch (Exception e) {
                        System.out.println("DEBUG: Failed to create image from profile picture data");
                        e.printStackTrace();
                        profilePicture = new Image(getClass().getResourceAsStream("/com/example/etradee/design/defaultpic1.jpg"));
                    }
                } else {
                    System.out.println("DEBUG: No profile picture data found. Using default image.");
                    profilePicture = new Image(getClass().getResourceAsStream("/com/example/etradee/design/defaultpic1.jpg"));
                }
                System.out.println("DEBUG: Final image loaded: " + (profilePicture != null));

                Image finalProfilePicture = profilePicture;
                javafx.application.Platform.runLater(() -> {
                    usernameLabel.setText(username);
                    roleLabel.setText(role);
                    contactLabel.setText(contactNum);
                    profilePictureImageView.setImage(finalProfilePicture);
                    profilePictureImageView.setFitWidth(300);  // Set the desired width
                    profilePictureImageView.setFitHeight(300); // Set the desired height
                    profilePictureImageView.setPreserveRatio(true);  // Maintain the aspect ratio
                    System.out.println("DEBUG: Profile picture set and adjusted to fit 300x300");
                });

                System.out.println("DEBUG: Profile fetched and displayed successfully for user: " + username);
            } else {
                System.out.println("DEBUG: No user found in database for username: " + loggedInUsername);
                javafx.application.Platform.runLater(() -> {
                    usernameLabel.setText("N/A");
                    roleLabel.setText("N/A");
                    contactLabel.setText("N/A");
                    Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/etradee/design/defaultpic1.jpg"));
                    profilePictureImageView.setImage(defaultImage);
                });
                System.out.println("DEBUG: Default values set due to user not found");
            }
        } catch (SQLException e) {
            System.out.println("ERROR: SQLException while fetching profile");
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("ERROR: Unexpected exception while fetching profile");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("DEBUG: Exiting loadUserDatabase method");
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
        // Restore the login window from minimized state
        if (loginStage != null) {
            loginStage.setIconified(false);
            loginStage.show();
        }
    }

    private void printAllUsers() {
        try {
            String query = "SELECT username, role, contactNum FROM users";
            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("DEBUG: All users in the database:");
                while (rs.next()) {
                    System.out.println("User: " + rs.getString("username") +
                            ", Role: " + rs.getString("role") +
                            ", Contact: " + rs.getString("contactNum"));
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Failed to print all users");
            e.printStackTrace();
        }
    }

    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(length, bytes.length); i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }

    private void printUserProfilePictureInfo() {
        try {
            String query = "SELECT username, LENGTH(profile_picture_path) as pic_length FROM users WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, loggedInUsername);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("DEBUG: User " + rs.getString("username") +
                                " profile picture length in database: " + rs.getInt("pic_length"));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Failed to print user profile picture info");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCashierButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/etradee/Cashier.fxml"));
            Parent cashierRoot = loader.load();

            CashierController cashierController = loader.getController();
            cashierController.setLoggedInUsername(loggedInUsername);
            cashierController.setPreviousScene(((Node) event.getSource()).getScene());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene cashierScene = new Scene(cashierRoot);
            stage.setScene(cashierScene);
            stage.setTitle("Cashier Window");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Show an error alert
        }
    }
}

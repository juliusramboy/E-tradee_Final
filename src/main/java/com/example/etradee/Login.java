package com.example.etradee;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.io.IOException;
import java.sql.*;

public class Login {
    @FXML
    private TextField usernametext;
    @FXML
    private PasswordField passwordtext;

    private Connection connection;

    public void initialize() {

        // Initialize the database connection
        try {
            connection = DatabaseCon.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernametext.getText();
        String password = passwordtext.getText();

        try {
            String query = "SELECT role FROM users WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String role = resultSet.getString("role");
                loadDashboard(username, role, (Stage) usernametext.getScene().getWindow());
            } else {
                // Invalid credentials
                showAlert("Login Failed", "Invalid username or password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while trying to log in");
        }
    }

    private void loadDashboard(String username, String role, Stage loginStage) {
        try {
            FXMLLoader loader;
            Parent root;
            if (role.equals("admin")) {
                loader = new FXMLLoader(getClass().getResource("admin_adduser.fxml"));
                root = loader.load();

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setTitle("Employee list");
                stage.initStyle(StageStyle.UTILITY);
                stage.setScene(scene);
                stage.show();

                TextField textField = new TextField();
                textField.setPromptText("Enter your username");
                textField.getStyleClass().add("prompt-text");

            } else {
                loader = new FXMLLoader(getClass().getResource("Profile_display_employee.fxml"));
                root = loader.load();

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setTitle("Employee Panel");
                stage.initStyle(StageStyle.UTILITY);
                stage.setScene(scene);
                stage.show();

                Profile_display_employee_controller controller= loader.getController();
                controller.setLoggedInUsername(username);
                controller.setLoginStage(loginStage);
            }


            // Hide the login stage after successful login
            loginStage.hide();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Loading Error", "An error occurred while trying to load the dashboard");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

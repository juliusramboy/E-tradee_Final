package com.example.etradee;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;

public class HelloController {
    @FXML
    private Button loginbutton;
    @FXML
    private PasswordField passwordtext;

    @FXML
    private TextField usernametext;

    private Connection connection;

  public void initialize(){

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
        String hashedPassword = hashPassword(password);

        if (hashedPassword == null) {
            showAlert("Error", "An error occurred while hashing the password.");
            return;
        }

        String query = "SELECT role FROM users WHERE username = '" + username + "' AND password = '" + password + "'";

        try (PreparedStatement statement = connection.prepareStatement(query)) {


            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String role = resultSet.getString("role");
                    loadDashboard(role, username);
                } else {
                    showAlert("Login Failed", "Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred during the login process.");
        }
    }

    private void loadDashboard(String role, String username) {
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root;
            if (role.equals("admin")) {
                loader.setLocation(getClass().getResource("admin_dashboard.fxml"));
                root = loader.load();
            } else {
                loader.setLocation(getClass().getResource("Profile_display_employee.fxml"));
                root = loader.load();

                // Set the username
                Profile_display_employee_controller controller = loader.getController();
                controller.setLoggedInUsername(username);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the dashboard.");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}


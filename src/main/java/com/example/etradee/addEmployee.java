package com.example.etradee;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class addEmployee {
    @FXML
    private TextField ADD_address;
    @FXML
    private Button ADD_cancelBtn;
    @FXML
    private Button ADD_confirmBtn;
    @FXML
    private TextField ADD_name;
    @FXML
    private TextField ADD_password;
    @FXML
    private TextField ADD_phonenumber;
    @FXML
    private ImageView ADD_profile;
    @FXML
    private ChoiceBox<String> ADD_role;
    @FXML
    private Button ADD_uploadBtn;
    @FXML
    private TextField ADD_username;

    private Connection connection;
    private Stage stage;
    private admin_addController adminController;
    private String selectedFilePath;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setAdminController(admin_addController adminController) {
        this.adminController = adminController;
    }

    @FXML
    void handlecancelADD(ActionEvent event) {
        Stage stage = (Stage) ADD_cancelBtn.getScene().getWindow();
        stage.close();
    }

    public void initialize() {
        ADD_role.getItems().addAll("admin", "employee");

        // Create a pattern that only allows integer input
        Pattern phoneNumber = Pattern.compile("-?\\d*");
        TextFormatter<String> phoneNumberFormatter = new TextFormatter<>(change -> {
            if (phoneNumber.matcher(change.getControlNewText()).matches()) {
                return change;
            } else {
                return null;
            }
        });

        // Set the formatter to the productAdd_quantity TextField
        ADD_phonenumber.setTextFormatter(phoneNumberFormatter);

        // Set default profile picture
        setDefaultProfilePicture();
    }

    @FXML
    void handleconfirmADD(ActionEvent event) {
        String name = ADD_name.getText();
        String username = ADD_username.getText();
        String password = ADD_password.getText();
        String role = ADD_role.getValue();
        String address = ADD_address.getText();
        String contact = ADD_phonenumber.getText();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || role == null || address.isEmpty() || contact.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }


        if (selectedFilePath == null || selectedFilePath.isEmpty()) {
            selectedFilePath = "/com/example/etradee/design/defaultpic1.jpg";
        }

        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username, employee_name, password, role, address, contactNum, profile_picture_path) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, username);
            statement.setString(2, name);
            statement.setString(3, password);
            statement.setString(4, role);
            statement.setString(5, address);
            statement.setString(6, contact);
            statement.setString(7, selectedFilePath);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                showSuccessAndClosePanel(event, "Employee added successfully.");
                if (adminController != null) {
                    adminController.refreshTable();
                }
            } else {
                showAlert("Error", "Failed to add employee.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to add employee: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closePanel(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage currentStage = (Stage) source.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void handleuploadADD(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                selectedFilePath = selectedFile.getAbsolutePath();
                Image image = new Image(selectedFile.toURI().toString());
                ADD_profile.setImage(image);
            } catch (Exception e) {
                showAlert("Error", "Failed to load the image: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // User canceled file selection, reset to default
            selectedFilePath = null;
            setDefaultProfilePicture();
        }
    }

    private void setDefaultProfilePicture() {
        Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/etradee/design/defaultpic1.jpg"));
        ADD_profile.setImage(defaultImage);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAndClosePanel(ActionEvent event, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        closePanel(event);
    }
}
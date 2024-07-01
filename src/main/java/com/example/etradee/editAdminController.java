package com.example.etradee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class editAdminController implements Initializable {

    private admin_addController.User user;
    private admin_addController adminController;
    private Stage viewPanelStage;
    private byte[] uploadedImageData;
    private Image defaultImage;

    @FXML
    private TextField EP_address;
    @FXML
    private Button EP_cancelBtn;
    @FXML
    private Button EP_confirmBtn;
    @FXML
    private TextField EP_name;
    @FXML
    private TextField EP_password;
    @FXML
    private TextField EP_phonenumber;
    @FXML
    private TextField EP_username;
    @FXML
    private Button EP_uploadBtn;
    @FXML
    private ImageView EP_imageprofile;
    @FXML
    private ChoiceBox<String> EP_role;

    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        EP_role.getItems().addAll("Admin", "Employee");
        //Q REGEX
        // Create a pattern that only allows integer input
        Pattern EditphoneNumber = Pattern.compile("-?\\d*");
        TextFormatter<String> EditphoneNumberFormatter = new TextFormatter<>(change -> {
            if (EditphoneNumber.matcher(change.getControlNewText()).matches()) {
                return change;
            } else {
                return null;
            }
        });

        // Set the formatter to the productAdd_quantity TextField
        EP_phonenumber.setTextFormatter(EditphoneNumberFormatter);

        InputStream imageStream = getClass().getResourceAsStream("/com/example/etradee/design/defaultpic1.jpg");
        if (imageStream != null) {
            defaultImage = new Image(imageStream);
            EP_imageprofile.setImage(defaultImage);
        } else {
            System.err.println("Default image not found!");
        }

    }

    public void setUserDetails(admin_addController.User user) {
        this.user = user;
        EP_name.setText(user.getEmployee_name());
        EP_username.setText(user.getUsername());
        EP_password.setText(user.getPassword());
        EP_phonenumber.setText(user.getContactNumber());
        EP_address.setText(user.getAddress());
        EP_role.setValue(user.getRole());

        byte[] profilePictureData = user.getProfilePictureData();
        if (profilePictureData != null && profilePictureData.length > 0) {
            System.out.println("Profile picture data length: " + profilePictureData.length);
            String firstBytes = bytesToHex(profilePictureData, 16);
            System.out.println("First 16 bytes: " + firstBytes);

            // Determine image format
            String format = "unknown";
            if (firstBytes.startsWith("FFD8")) {
                format = "jpg";
            } else if (firstBytes.startsWith("89504E47")) {
                format = "png";
            } else if (firstBytes.startsWith("47494638")) {
                format = "gif";
            }
            System.out.println("Detected image format: " + format);

            try {
                javafx.application.Platform.runLater(() -> {
                    try {
                        Image image = new Image(new ByteArrayInputStream(profilePictureData));
                        if (!image.isError()) {
                            EP_imageprofile.setImage(image);
                            System.out.println("Profile picture loaded for user: " + user.getEmployee_name());
                        } else {
                            System.err.println("Error in image data for user: " + user.getEmployee_name());
                            EP_imageprofile.setImage(defaultImage);
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading profile picture for user: " + user.getEmployee_name());
                        e.printStackTrace();
                        EP_imageprofile.setImage(defaultImage);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error in runLater for user: " + user.getEmployee_name());
                e.printStackTrace();
                EP_imageprofile.setImage(defaultImage);
            }
        } else {
            System.out.println("No profile picture data for user: " + user.getEmployee_name() + ". Using default image.");
            EP_imageprofile.setImage(defaultImage);
        }
    }


    public void setAdminAddController(admin_addController adminController) {
        this.adminController = adminController;
    }

    public void setViewPanelStage(Stage viewPanelStage) {

        this.viewPanelStage = viewPanelStage;

    }

    @FXML
    void handleCancelAction(ActionEvent event) {
        closeWindow();
    }

    @FXML
    @FXMl
    void handleConfirmAction(ActionEvent event) {
        if (user != null) {
            try (Connection connection = DatabaseCon.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE users SET username = ?, password = ?, role = ?, employee_name = ?, address = ?, contactNum = ?, profile_picture_path = ? WHERE id = ?")) {

                // Set all parameters
                statement.setString(1, EP_username.getText());
                statement.setString(2, EP_password.getText());
                statement.setString(3, EP_role.getValue());
                statement.setString(4, EP_name.getText());
                statement.setString(5, EP_address.getText());
                statement.setString(6, EP_phonenumber.getText());

                // Handle the profile picture
                if (uploadedImageData != null) {
                    statement.setBytes(7, uploadedImageData);
                    System.out.println("Saving uploaded image data. Length: " + uploadedImageData.length);
                } else if (user.getProfilePictureData() != null && user.getProfilePictureData().length > 0) {
                    statement.setBytes(7, user.getProfilePictureData());
                    System.out.println("Saving existing profile picture data.");
                } else {
                    // If no image is uploaded and no existing image, use the default image
                    try (InputStream defaultImageStream = getClass().getResourceAsStream("/com/example/etradee/design/defaultpic1.jpg")) {
                        if (defaultImageStream != null) {
                            byte[] defaultImageData = defaultImageStream.readAllBytes();
                            statement.setBytes(7, defaultImageData);
                            System.out.println("Saving default image data. Length: " + defaultImageData.length);
                        } else {
                            statement.setNull(7, java.sql.Types.BLOB);
                            System.out.println("No image data available. Setting to NULL.");
                        }
                    }
                }

                // Set the ID for the WHERE clause
                statement.setString(8, user.getId());

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("User details updated successfully.");
                    showAlert(Alert.AlertType.INFORMATION, "User Updated", "User details have been updated.");

                    if (adminController != null) {
                        adminController.refreshTable();
                    }

                    closeWindow();
                    if (viewPanelStage != null) {
                        viewPanelStage.close();
                    }
                } else {
                    System.out.println("Failed to update user details. No rows affected.");
                }
            } catch (SQLException | IOException e) {
                System.err.println("Error updating user details: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user details. Please try again.");
            }
        } else {
            System.out.println("User object is null. Cannot update details.");
            showAlert(Alert.AlertType.WARNING, "Warning", "User object is null. Cannot update details.");
        }
    }

    @FXML
    void handleUploadAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog((Stage) EP_uploadBtn.getScene().getWindow());

        if (file != null) {
            try {
                uploadedImageData = Files.readAllBytes(file.toPath());
                System.out.println("Uploaded image data length: " + uploadedImageData.length);
                System.out.println("First 8 bytes of uploaded image: " + bytesToHex(uploadedImageData, 8));

                // Load and display the image
                Image image = new Image(new ByteArrayInputStream(uploadedImageData));
                EP_imageprofile.setImage(image);

                showAlert(Alert.AlertType.INFORMATION, "Upload Successful", "Profile picture uploaded successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Upload Failed", "Failed to upload profile picture. Please try again.");
            }
        }
    }

    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(length, bytes.length); i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) EP_cancelBtn.getScene().getWindow();
        stage.close();
    }
}
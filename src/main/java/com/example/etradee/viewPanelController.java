package com.example.etradee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class viewPanelController implements Initializable {

    private admin_addController.User user;
    private admin_addController adminController;
    private Image imagedef;

    @FXML private TextField VP_address;
    @FXML private Button VP_backBtn;
    @FXML private Button VP_deleteBtn;
    @FXML private Button VP_editBtn;
    @FXML private TextField VP_name;
    @FXML private TextField VP_password;
    @FXML private TextField VP_phonenumber;
    @FXML private ImageView VP_profile;
    @FXML private TextField VP_role;
    @FXML private TextField VP_username;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Additional initialization if needed
    }

    private void loadDefaultImage() {
        try {
            InputStream imgs = getClass().getResourceAsStream("/com/example/etradee/design/defaultpic1.jpg");
            imagedef = new Image(imgs);
            VP_profile.setImage(imagedef);
            System.out.println("Default image loaded successfully.");
        } catch (Exception e) {
            System.out.println("Error loading default image");
            e.printStackTrace();
        }
    }

    public void setUserDetails(admin_addController.User user) {
        this.user = user;
        VP_name.setText(user.getEmployee_name());
        VP_username.setText(user.getUsername());
        VP_password.setText(user.getPassword());
        VP_phonenumber.setText(user.getContactNumber());
        VP_address.setText(user.getAddress());
        VP_role.setText(user.getRole());

        byte[] profilePictureData = user.getProfilePictureData();
        if (profilePictureData != null && profilePictureData.length > 0) {
            System.out.println("Profile picture data length: " + profilePictureData.length);
            String format = determineImageFormat(profilePictureData);
            System.out.println("Detected image format: " + format);

            saveImageDataToFile(profilePictureData, user.getId());

            try {
                Image profileImage;
                if (format.equals("Unknown")) {
                    // Try to interpret the data as a file path
                    String imagePath = new String(profilePictureData);
                    profileImage = new Image("file:" + imagePath);
                } else {
                    InputStream inputStream = new ByteArrayInputStream(profilePictureData);
                    profileImage = new Image(inputStream);
                }

                if (!profileImage.isError()) {
                    VP_profile.setImage(profileImage);
                    System.out.println("Profile picture loaded successfully for user: " + user.getEmployee_name());
                } else {
                    System.out.println("Error loading profile image: " + profileImage.getException());
                    loadDefaultImage();
                }
            } catch (Exception e) {
                System.out.println("Error creating image from profile picture data for user: " + user.getEmployee_name());
                e.printStackTrace();
                loadDefaultImage();
            }
        } else {
            System.out.println("No profile picture data available for user: " + user.getEmployee_name());
            loadDefaultImage();
        }
    }
    private void saveImageDataToFile(byte[] imageData, String userId) {
        try {
            String fileName = "user_" + userId + "_profile_pic.dat";
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                fos.write(imageData);
            }
            System.out.println("Image data saved to file: " + fileName);
        } catch (IOException e) {
            System.out.println("Error saving image data to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String determineImageFormat(byte[] imageData) {
        if (imageData.length >= 8) {
            System.out.println("First 8 bytes of image data: " +
                    String.format("%02X %02X %02X %02X %02X %02X %02X %02X",
                            imageData[0], imageData[1], imageData[2], imageData[3],
                            imageData[4], imageData[5], imageData[6], imageData[7]));

            if (imageData[0] == (byte)0xFF && imageData[1] == (byte)0xD8) {
                return "JPEG";
            } else if (imageData[0] == (byte)0x89 && imageData[1] == (byte)0x50) {
                return "PNG";
            } else if (imageData[0] == (byte)0x47 && imageData[1] == (byte)0x49) {
                return "GIF";
            } else {
                System.out.println("Data as string: " + new String(imageData));
                return "Unknown";
            }
        }
        return "Unknown (data length < 8 bytes)";
    }
    private void loadImageWithSwingBufferedImage(byte[] imageData) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (bufferedImage != null) {
                WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                VP_profile.setImage(fxImage);
                System.out.println("Image loaded successfully using BufferedImage");
            } else {
                System.out.println("BufferedImage is null");
                loadDefaultImage();
            }
        } catch (IOException e) {
            System.out.println("Error loading image with BufferedImage: " + e.getMessage());
            e.printStackTrace();
            loadDefaultImage();
        }
    }

    public void setAdminController(admin_addController adminController) {
        this.adminController = adminController;
    }

    @FXML
    void handleBackAction(ActionEvent event) {
        closeWindow();
    }

    @FXML
    void handleDeleteAction(ActionEvent event) {
        if (user != null) {
            try (Connection connection = DatabaseCon.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
                statement.setString(1, user.getId());
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("User deleted successfully.");
                    showAlert(Alert.AlertType.INFORMATION, "User is Deleted", "The user has been deleted");
                    closeWindow();

                    // Refresh table in admin_addController
                    if (adminController != null) {
                        adminController.refreshTable();
                    }
                } else {
                    System.out.println("Failed to delete user. No rows affected.");
                }
            } catch (SQLException e) {
                System.err.println("Error deleting user: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user. Please try again.");
            }
        } else {
            System.out.println("User object is null. Cannot delete.");
            showAlert(Alert.AlertType.WARNING, "Warning", "User object is null. Cannot delete.");
        }
    }

    @FXML
    void handleEditAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editPanel.fxml"));
            Parent root = loader.load();

            editAdminController controller = loader.getController();
            controller.setUserDetails(user);
            controller.setAdminAddController(adminController);
            controller.setViewPanelStage((Stage) VP_editBtn.getScene().getWindow());

            Stage stage = new Stage();
            stage.setTitle("Edit employee");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.show();
            System.out.println("Edit panel opened for user: " + user.getEmployee_name());

        } catch (IOException e) {
            System.out.println("Error loading edit panel for user: " + user.getEmployee_name());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) VP_backBtn.getScene().getWindow();
        stage.close();
        System.out.println("View panel closed.");
    }
}

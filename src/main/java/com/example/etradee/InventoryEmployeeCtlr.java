package com.example.etradee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class InventoryEmployeeCtlr {

    @FXML
    private Button ie_backBtn;

    @FXML
    private TableColumn<?, ?> ie_itemName;

    @FXML
    private TableColumn<?, ?> ie_itemNum;

    @FXML
    private TableColumn<?, ?> ie_price;

    @FXML
    private TableColumn<?, ?> ie_quality;

    @FXML
    private TableColumn<?, ?> ie_quantity;

    @FXML
    private TextField ie_search;

    @FXML
    private TableView<?> product_table;
    private Scene previousScene;
    private String loggedInUsername;
    /////////////////////////////////////////////////////////Back button///////////////////////////////////////////////////////////
    @FXML
    void ie_backBtn_handle(ActionEvent event) {
        if (previousScene != null) {
            Stage stage = (Stage) ie_backBtn.getScene().getWindow();
            stage.setScene(previousScene);
            stage.setTitle("Employee Profile");
            stage.show();
        } else {
            // Fallback in case previousScene is null
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/etradee/Profile_display_employee.fxml"));
                Parent profileRoot = loader.load();

                Profile_display_employee_controller profileController = loader.getController();
                profileController.setLoggedInUsername(loggedInUsername);

                Stage stage = (Stage) ie_backBtn.getScene().getWindow();
                Scene profileScene = new Scene(profileRoot);
                stage.setScene(profileScene);
                stage.setTitle("Employee Profile");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                //showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to the profile view.");
            }
        }
    }

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        //System.out.println("DEBUG: setLoggedInUsername called with: " + loggedInUsername);
        //fetchEmployeeDetails();
    }

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }


    /////////////////////////////////////////////////////////Back button///////////////////////////////////////////////////////////
}
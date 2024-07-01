package com.example.etradee;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.naming.PartialResultException;
import java.io.IOException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class SalesEmployeeCtlr implements Initializable {

    @FXML
    private Button Employee_backBtn;

    @FXML
    private TableColumn<Order, String>  e_date;

    @FXML
    private TableColumn<Order, String>  e_item;

    @FXML
    private TableColumn<Order, String> e_price;

    @FXML
    private TableColumn<Order, String> e_quality;

    @FXML
    private TableColumn<Order, String> e_quantity;

    @FXML
    private TextField Employee_search;

    @FXML
    private TableColumn<Order, String> e_time;

    @FXML
    private TableColumn<Order, String> e_transDetails;

    @FXML
    private TableColumn<Order, String> e_transnum;

    @FXML
    private TableView<Order> employeeTableView;

    @FXML
    private Label currentDateLabel;

    @FXML
    private Label currentTimeLabel;

    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private Connection connection;
    /////////////////////////////////////////////////////////////////////
    private String loggedInUsername;
    private Profile_display_employee_controller previousController;
    private Scene previousScene;

    @FXML
    void Employee_backBtn_handle(ActionEvent event) {
        if (previousScene != null) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene profileScene = new Scene(profileRoot);
                stage.setScene(profileScene);
                stage.setTitle("Employee Profile");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
        loadEmployeeData(); // Assuming this method loads sales data
    }

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        e_time.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime()));
        e_date.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        e_item.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemPurchased()));
        e_quantity.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuantity()));
        e_quality.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuality()));
        e_price.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrice()));
        e_transnum.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionNumber()));
        e_transDetails.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionDetails()));

        employeeTableView.setItems(orders);

        try {
            connection = DatabaseCon.getConnection();
            loadEmployeeData();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Employee_search.textProperty().addListener((observable, oldValue, newValue) -> searchEmployeeData(newValue));
    }

    private void loadEmployeeData() {
        String query = "SELECT purchase_time, purchase_date, item_purchased, quantity, price, transaction_number, transaction_details,  quality FROM confirmed_orders";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String time = resultSet.getString("purchase_time");
                String date = resultSet.getString("purchase_date");
                String itemPurchased = resultSet.getString("item_purchased");
                String quantity = resultSet.getString("quantity");
                String price = resultSet.getString("price");
                String transactionNumber = resultSet.getString("transaction_number");
                String transactionDetails = resultSet.getString("transaction_details");
                String quality = resultSet.getString("quality");

                orders.add(new Order(null, time, date, itemPurchased, quantity, price, transactionNumber, transactionDetails, quality));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchEmployeeData(String searchQuery) {
        orders.clear();
        String query = "SELECT purchase_date, item_purchased, price, quality, quantity, purchase_time, transaction_number, transaction_details FROM confirmed_orders WHERE transaction_number LIKE ? OR purchase_date LIKE ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, "%" + searchQuery + "%");
            preparedStatement.setString(2, "%" + searchQuery + "%");

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String date = resultSet.getString("purchase_date");
                String itemPurchased = resultSet.getString("item_purchased");
                String price = resultSet.getString("price");
                String quality = resultSet.getString("quality");
                String quantity = resultSet.getString("quantity");
                String time = resultSet.getString("purchase_time");
                String transactionNumber = resultSet.getString("transaction_number");
                String transactionDetails = resultSet.getString("transaction_details");

                orders.add(new Order(null, time, date, itemPurchased, quantity, price, transactionNumber, transactionDetails, quality));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

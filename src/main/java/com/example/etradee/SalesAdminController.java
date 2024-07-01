package com.example.etradee;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class SalesAdminController implements Initializable {

    @FXML
    private TableView<Order> salesTableView;

    @FXML
    private Button Sales_Employeelist;

    @FXML
    private Button Sales_Productlist;

    @FXML
    private Button Sales_Sales;

    @FXML
    private Button Sales_logoutBtn;

    @FXML
    private TextField Sales_search;

    @FXML
    private TableColumn<Order, String> account_name;

    @FXML
    private TableColumn<Order, String> time;

    @FXML
    private TableColumn<Order, String> purchase_date;

    @FXML
    private TableColumn<Order, String> item_purchased;

    @FXML
    private TableColumn<Order, String> quantity;

    @FXML
    private TableColumn<Order, String> price;

    @FXML
    private TableColumn<Order, String> transaction_number;

    @FXML
    private TableColumn<Order, String> transaction_details;

    @FXML
    private Button sales_admin;

    private ObservableList<Order> orders = FXCollections.observableArrayList();

    private Connection connection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set cell value factories for each TableColumn
        account_name.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAccountName()));
        time.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime()));
        purchase_date.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        item_purchased.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemPurchased()));
        quantity.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuantity()));
        price.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrice()));
        transaction_number.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionNumber()));
        transaction_details.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionDetails()));

        salesTableView.setItems(orders); // Set items to the TableView



        try {
            connection = DatabaseCon.getConnection(); // Get database connection
            loadSalesData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSalesData() {
        String query = "SELECT account_name, purchase_time, purchase_date, item_purchased, quantity, price, transaction_number, transaction_details FROM confirmed_orders";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                // Retrieve data from ResultSet
                String accountName = resultSet.getString("account_name");
                String time = resultSet.getString("purchase_time");
                String date = resultSet.getString("purchase_date");
                String itemPurchased = resultSet.getString("item_purchased");
                String quantity = resultSet.getString("quantity");
                String price = resultSet.getString("price");
                String transactionNumber = resultSet.getString("transaction_number");
                String transactionDetails = resultSet.getString("transaction_details");

                // Create Order object and add to ObservableList
                orders.add(new Order(accountName, time, date, itemPurchased, quantity, price, transactionNumber, transactionDetails, transactionDetails)); // Ensure the Order constructor parameters are in the correct order
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Sales_search.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                salesTableView.setItems(orders);
            } else {
                performSearch(newValue.trim());
            }
        });

    }

    @FXML
    void Sales_Employeelist_handle(ActionEvent event) {
        Stage stage = (Stage) Sales_logoutBtn.getScene().getWindow(); // Use any UI component to get the current stage
        stage.close(); // Close the current stage (admin_add window)

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_adduser.fxml"));
            Parent root = loader.load();

            // SalesAdminController controller = loader.getController();
            // You can set any necessary data or context to the controller here if needed

            Stage newStage = new Stage();
            newStage.setTitle("Employee List");
            newStage.setScene(new Scene(root));
            newStage.initStyle(StageStyle.UTILITY);
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    void Sales_Productlist_handle(ActionEvent event) {
        Stage stage = (Stage) Sales_logoutBtn.getScene().getWindow(); // Use any UI component to get the current stage
       stage.close(); // Close the current stage (admin_add window)

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("productListpanel.fxml"));
            Parent root = loader.load();

            // SalesAdminController controller = loader.getController();
            // You can set any necessary data or context to the controller here if needed

            Stage newStage = new Stage();
            newStage.setTitle("Product List");
            newStage.setScene(new Scene(root));
            newStage.initStyle(StageStyle.UTILITY);
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    void Sales_Sales_handle(ActionEvent event) {

    }

    @FXML
    void Sales_logout_handle(ActionEvent event) {

        Stage stage = (Stage) Sales_logoutBtn.getScene().getWindow(); // Use any UI component to get the current stage
        stage.close(); // Close the current stage (admin_add window)

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();

            // SalesAdminController controller = loader.getController();
            // You can set any necessary data or context to the controller here if needed

            Stage newStage = new Stage();
            newStage.setTitle("E-Trade");
            newStage.setScene(new Scene(root));
            newStage.initStyle(StageStyle.UTILITY);
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Sales_search_handle(ActionEvent event) {

        String searchText = Sales_search.getText().trim();
        if (!searchText.isEmpty()) {
            performSearch(searchText);
        } else {
            salesTableView.setItems(orders);
        }

    }

    private void performSearch(String searchText) {
        ObservableList<Order> filteredList = FXCollections.observableArrayList();
        for (Order order : orders) {
            if (order.getAccountName().toLowerCase().contains(searchText.toLowerCase()) ||
                    order.getItemPurchased().toLowerCase().contains(searchText.toLowerCase()) ||
                    order.getTransactionNumber().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(order);
            }
        }
        salesTableView.setItems(filteredList);
    }
}

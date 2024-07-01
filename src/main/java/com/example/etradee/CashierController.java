package com.example.etradee;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TextFormatter;
import javafx.stage.StageStyle;

import javafx.scene.control.Alert;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

import javafx.print.PrinterJob;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;

public class CashierController implements Initializable {

    @FXML
    private Button cashier_backBtn;

    @FXML
    private Label cashier_change;

    @FXML
    private Button cashier_confirmBtn;

    @FXML
    private Label cashier_date;

    @FXML
    private Button cashier_deleteBtn;

    @FXML
    private TextField cashier_money;

    @FXML
    private Label cashier_name;

    @FXML
    private Label cashier_position;

    @FXML
    private TextField cashier_search;

    @FXML
    private TableView<Product> cashier_tableLeftt;

    @FXML
    private TableView<Product> cashier_tableRight;

    @FXML
    private Label cashier_time;

    @FXML
    private Label cashier_totalFee;

    private Connection connection;
    private Stage primaryStage;
    private Product product;
    private String username;
    private String role;
    private String loggedInUsername;
    private Scene previousScene;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        Pattern money = Pattern.compile("-?\\d*");
        TextFormatter<String> moneyFormatter = new TextFormatter<>(change -> {
            if (money.matcher(change.getControlNewText()).matches()) {
                return change;
            } else {
                return null;
            }
        });
        // Set the formatter to the productAdd_quantity TextField
        cashier_money.setTextFormatter(moneyFormatter);

        try {
            connection = DatabaseCon.getConnection(); // Adjust this to your database connection method
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
        }

        cashier_money.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                calculateChange();
            } else {
                cashier_change.setText("₱0.00");
            }
        });

        fetchEmployeeDetails(); // Fetch and display employee details
        cashier_date.setText(currentDate.toString());
        cashier_time.setText(currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));


        initializeUI();
        loadInitialData();
        setupEventHandlers();
    }


    private void initializeUI() {
        // Left table setup
        TableColumn<Product, String> nameColumn = (TableColumn<Product, String>) cashier_tableLeftt.getColumns().get(0);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Double> priceColumn = (TableColumn<Product, Double>) cashier_tableLeftt.getColumns().get(1);
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Integer> quantityColumn = (TableColumn<Product, Integer>) cashier_tableLeftt.getColumns().get(2);
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, String> qualityColumn = (TableColumn<Product, String>) cashier_tableLeftt.getColumns().get(3);
        qualityColumn.setCellValueFactory(new PropertyValueFactory<>("quality"));

        // Right table setup
        TableColumn<Product, String> rightNameColumn = (TableColumn<Product, String>) cashier_tableRight.getColumns().get(0);
        rightNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Integer> rightQuantityColumn = (TableColumn<Product, Integer>) cashier_tableRight.getColumns().get(1);
        rightQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    private void loadInitialData() {
        loadProducts();
    }

    private void setupEventHandlers() {
        cashier_backBtn.setOnAction(event -> cashier_backBtn_handle());
        cashier_deleteBtn.setOnAction(event -> cashier_delete_handle());
        cashier_search.textProperty().addListener((observable, oldValue, newValue) -> filterLeftTable(newValue));

        // Add double-click event handler to the left table
        cashier_tableLeftt.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Product selectedProduct = cashier_tableLeftt.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    addProductToRightTable(selectedProduct);
                }
            }
        });

        cashier_confirmBtn.setOnAction(event -> cashier_confirm_handle()); // Add this line
    }

    private void filterLeftTable(String newValue) {
    }

    private void addProductToRightTable(Product selectedProduct) {
        for (Product existingProduct : cashier_tableRight.getItems()) {
            if (existingProduct.getId() == selectedProduct.getId()) {
                // If it exists, increment the quantity
                existingProduct.setQuantity(existingProduct.getQuantity() + 1);
                cashier_tableRight.refresh();
                updateTotalFee();
                return;
            }
        }

        // If it doesn't exist, add a new product with quantity 1
        Product newProduct = new Product(selectedProduct.getId(), selectedProduct.getName(), selectedProduct.getPrice(), 1, selectedProduct.getQuality());
        cashier_tableRight.getItems().add(newProduct);
        updateTotalFee();
    }
    /////////////////////////////////////////////////////Back button///////////////////////////////////////
    @FXML
    private void cashier_backBtn_handle() {
        if (previousScene != null) {
            Stage stage = (Stage) cashier_backBtn.getScene().getWindow();
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

                Stage stage = (Stage) cashier_backBtn.getScene().getWindow();
                Scene profileScene = new Scene(profileRoot);
                stage.setScene(profileScene);
                stage.setTitle("Employee Profile");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to return to the profile view.");
            }
        }
    }

    // Add this method to set the previous scene
    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }
    /////////////////////////////////////////////////////Back button///////////////////////////////////////

    @FXML
    private void cashier_delete_handle() {
        // Prompt for admin password
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Admin access");
        dialog.setHeaderText("Enter admin password:");

        // Set the button types
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // Create layout and add password field
        VBox vbox = new VBox();
        vbox.getChildren().addAll(new Label("Password:"), passwordField);
        dialog.getDialogPane().setContent(vbox);

        // Request focus on the password field by default
        Platform.runLater(passwordField::requestFocus);

        // Convert the result to the password when the login button is clicked

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return passwordField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            try {
                // Query the database to check if the password matches an admin's password
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM users WHERE role = 'admin' AND password = ?");
                statement.setString(1, password);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Password is correct and user is admin, proceed with deleting the order
                    Product selectedProduct = cashier_tableRight.getSelectionModel().getSelectedItem();
                    if (selectedProduct != null) {
                        cashier_tableRight.getItems().remove(selectedProduct);
                        updateTotalFee();
                    }
                } else {
                    // Password is incorrect or user is not admin, show error message
                    showAlert(Alert.AlertType.ERROR, "Authorization Failed", "You are not authorized to delete orders.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting the order.");
            }
        });
    }
    private void printReceipt(TextFlow receiptTextFlow) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(primaryStage)) {
            boolean success = printerJob.printPage(receiptTextFlow);
            if (success) {
                printerJob.endJob();
            }
        }
    }


    private String generateTransactionNumber() {
        // Implement your logic to generate a unique transaction number
        return "TXN-" + System.currentTimeMillis(); // Example implementation
    }

    @FXML
    private void cashier_confirm_handle() {
        // Get the employee's account name


        String accountName = cashier_name.getText();
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        String transactionNumber = generateTransactionNumber();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Receipt.fxml"));
            Parent root = loader.load();

            ReceiptController receiptController = loader.getController();
            Stage previewStage = new Stage();
            previewStage.setTitle("Receipt");
            receiptController.setPreviewStage(previewStage);

            double totalFee = cashier_tableRight.getItems().stream()
                    .mapToDouble(product -> product.getPrice() * product.getQuantity())
                    .sum();

            double moneyReceived = Double.parseDouble(cashier_money.getText().trim());
            double change = moneyReceived - totalFee;

            receiptController.setReceiptData(transactionNumber, accountName, currentDate, currentTime, cashier_tableRight.getItems(), totalFee, change);

            Scene scene = new Scene(root);
            previewStage.setScene(scene);
            previewStage.initModality(Modality.APPLICATION_MODAL);
            previewStage.showAndWait();

            // Insert the order details into the confirmed_orders table and clear the table
            for (Product product : cashier_tableRight.getItems()) {
                String itemPurchased = product.getName();
                int quantity = product.getQuantity();
                String quality = product.getQuality();
                double price = product.getPrice();

                String insertQuery = "INSERT INTO confirmed_orders (account_name, purchase_time, purchase_date, item_purchased, quantity, quality, price, transaction_number, transaction_details) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                preparedStatement.setString(1, accountName);
                preparedStatement.setTime(2, Time.valueOf(currentTime));
                preparedStatement.setDate(3, Date.valueOf(currentDate));
                preparedStatement.setString(4, itemPurchased);
                preparedStatement.setInt(5, quantity);
                preparedStatement.setString(6, quality);
                preparedStatement.setDouble(7, price);
                preparedStatement.setString(8, transactionNumber);
                preparedStatement.setString(9, "Paid");

                preparedStatement.executeUpdate();
            }

            cashier_tableRight.getItems().clear();
            updateTotalFee();
            showAlert(Alert.AlertType.INFORMATION, "Order Confirmed", "The order has been confirmed and saved successfully.");

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while generating the receipt.");
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Set header text to null to remove the default header
        alert.setContentText(content);
        alert.showAndWait(); // Wait for user to close the alert dialog
    }

    private void loadProducts() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id_product, product_name, price, quantity, quality FROM product_list");

            while (resultSet.next()) {
                int id = resultSet.getInt("id_product");
                String name = resultSet.getString("product_name");
                double price = resultSet.getDouble("price");
                int quantity = resultSet.getInt("quantity");
                String quality = resultSet.getString("quality");

                Product product = new Product(id, name, price, quantity, quality);
                cashier_tableLeftt.getItems().add(product);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTotalFee() {
        double total = cashier_tableRight.getItems().stream()
                .mapToDouble(product -> product.getPrice() * product.getQuantity())
                .sum();
        cashier_totalFee.setText(String.format("₱%.2f", total));
        calculateChange();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void calculateChange() {
        try {
            double totalFee = Double.parseDouble(cashier_totalFee.getText().replace("₱", "").trim());
            double moneyReceived = Double.parseDouble(cashier_money.getText().trim());
            double change = moneyReceived - totalFee;

            if (change >= 0) {
                cashier_change.setText(String.format("₱%.2f", change));
            } else {
                cashier_change.setText("Insufficient Money");
            }
        } catch (NumberFormatException e) {
            cashier_change.setText("invalid");
        }
    }

    private void fetchEmployeeDetails() {
        try {
            String query = "SELECT username, role FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, loggedInUsername);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                username = resultSet.getString("username");
                role = resultSet.getString("role");

                cashier_name.setText(username);
                cashier_position.setText(role);
            } else {
                //showAlert(Alert.AlertType.ERROR, "User Not Found", "The logged-in user's details could not be found.");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while fetching user details.");
        }
    }


    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        System.out.println("DEBUG: setLoggedInUsername called with: " + loggedInUsername);
        fetchEmployeeDetails();
    }
}

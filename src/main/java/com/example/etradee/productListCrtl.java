package com.example.etradee;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class productListCrtl implements Initializable {

    @FXML
    private TableColumn<Product, Integer> IDitems_Column;
    @FXML
    private TableColumn<Product, String> Nameofitems_Column;
    @FXML
    private TableColumn<Product, Double> Price_Column;
    @FXML
    private TableColumn<Product, String> Quality_Column;
    @FXML
    private TableColumn<Product, Integer> Quantity_Column;
    @FXML
    private Button product_addBtn;
    @FXML
    private Label product_date;
    @FXML
    private Button product_employee;
    @FXML
    private Button product_logout;
    @FXML
    private Button product_productlist;
    @FXML
    private Button product_sales;
    @FXML
    private TextField product_search;
    @FXML
    private TableView<Product> product_table;
    @FXML
    private Label product_time;

//    @FXML
//    private Button product_list;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Product> filteredProductList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTable();
        loadProductData();
        filteredProductList.setAll(productList);
        product_table.setItems(filteredProductList);
        updateDateTime();

        product_search.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProductList(newValue);
        });
    }

    private void initializeTable() {
        IDitems_Column.setCellValueFactory(new PropertyValueFactory<>("idProduct"));
        Nameofitems_Column.setCellValueFactory(new PropertyValueFactory<>("productName"));
        Price_Column.setCellValueFactory(new PropertyValueFactory<>("price"));
        Quantity_Column.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        Quality_Column.setCellValueFactory(new PropertyValueFactory<>("quality"));

        product_table.setItems(productList);

        product_table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Product selectedProduct = product_table.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    openViewProductWindow(selectedProduct);
                }
            }
        });
    }

    private void loadProductData() {
        productList.clear();
        try (Connection connection = DatabaseCon.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM product_list")) {

            while (resultSet.next()) {
                Product product = new Product(
                        resultSet.getInt("id_product"),
                        resultSet.getString("product_name"),
                        resultSet.getDouble("price"),
                        resultSet.getInt("quantity"),
                        resultSet.getString("quality")
                );
                productList.add(product);
            }

            // Ensure the UI update happens on the JavaFX Application Thread
            javafx.application.Platform.runLater(() -> {
                filteredProductList.setAll(productList);
                product_table.setItems(filteredProductList);
            });
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message)
        }
    }

    private void updateDateTime() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        product_date.setText(currentDate.format(dateFormatter));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalTime currentTime = LocalTime.now();
            product_time.setText(currentTime.format(timeFormatter));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    @FXML
    void product_addproduct_handle(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addproductPanel.fxml"));
            Parent root = loader.load();

            addProductController controller = loader.getController();
            controller.setProductListController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Product");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message)
        }
    }

    @FXML
    void handleRowDoubleClick() {
        Product selectedProduct = product_table.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            openViewProductWindow(selectedProduct);
        }
    }

    private void openViewProductWindow(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("viewproducPanel.fxml"));
            Parent root = loader.load();

            viewProductController controller = loader.getController();
            controller.setProductListController(this);
            controller.setProduct(product);

            Stage stage = new Stage();
            stage.setTitle("View/Edit Product");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message)
        }
    }


    @FXML
    void product_search_handle(ActionEvent event) {
        String searchText = product_search.getText();
        filterProductList(searchText);
    }

    private void filterProductList(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredProductList.setAll(productList);
        } else {
            filteredProductList.clear();
            String lowerCaseFilter = searchText.toLowerCase();
            for (Product product : productList) {
                if (product.getProductName().toLowerCase().contains(lowerCaseFilter)) {
                    filteredProductList.add(product);
                }
            }
        }
        product_table.setItems(filteredProductList);
    }

    @FXML
    void product_employee_handle(ActionEvent event) {
        Stage stage = (Stage) product_logout.getScene().getWindow(); // Use any UI component to get the current stage
        stage.close(); // Close the current stage (product list window)

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_adduser.fxml"));
            Parent root = loader.load();

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
    void product_logout_handle(ActionEvent event) {
         Stage stage = (Stage) product_logout.getScene().getWindow(); // Use any UI component to get the current stage
         stage.close(); // Close the current stage (product list window)

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("E-trade");
            newStage.setScene(new Scene(root));
            newStage.initStyle(StageStyle.UTILITY);
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void product_product_handle(ActionEvent event) {
        loadProductData();
    }

    @FXML
    void product_sales_handle(ActionEvent event) {
        Stage stage = (Stage) product_logout.getScene().getWindow(); // Use any UI component to get the current stage
        stage.close(); // Close the current stage (admin_add window)

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesAdmin.fxml"));
            Parent root = loader.load();

            // SalesAdminController controller = loader.getController();
            // You can set any necessary data or context to the controller here if needed

            Stage newStage = new Stage();
            newStage.setTitle("Sales Administration");
            newStage.setScene(new Scene(root));
            newStage.initStyle(StageStyle.UTILITY);
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshProductList() {
        loadProductData();
        filterProductList(product_search.getText());
    }

    public Product getProductById(int idProduct) {
        return null;
    }

    // Product class to represent data
    public static class Product {
        private int idProduct;
        private String productName;
        private double price;
        private int quantity;
        private String quality;

        public Product(int idProduct, String productName, double price, int quantity, String quality) {
            this.idProduct = idProduct;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.quality = quality;
        }

        // Getters
        public int getIdProduct() { return idProduct; }
        public String getProductName() { return productName; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public String getQuality() { return quality; }

        // Setters
        public void setIdProduct(int idProduct) { this.idProduct = idProduct; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setPrice(double price) { this.price = price; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setQuality(String quality) { this.quality = quality; }
    }
}

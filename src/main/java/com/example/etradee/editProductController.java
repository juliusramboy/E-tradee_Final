package com.example.etradee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class editProductController {
    @FXML
    private TextField editProduct_name;
    @FXML
    private TextField editProduct_price;
    @FXML
    private TextField editProduct_quality;
    @FXML
    private TextField editProduct_quantity;
    @FXML
    private Button editProduct_confirmBtn;
    @FXML
    private Button editProduct_cancelBtn;

    private productListCrtl productListController;
    private productListCrtl.Product currentProduct;

    public void setProductListController(productListCrtl controller) {
        this.productListController = controller;
    }

    public void setProduct(productListCrtl.Product product) {
        this.currentProduct = product;
        populateFields();
    }

    private void populateFields() {
        if (currentProduct != null) {
            editProduct_name.setText(currentProduct.getProductName());
            editProduct_price.setText(String.valueOf(currentProduct.getPrice()));
            editProduct_quality.setText(currentProduct.getQuality());
            editProduct_quantity.setText(String.valueOf(currentProduct.getQuantity()));
        }
    }
    @FXML
    public void initialize() {
        //Quantity REGEX
        // Create a pattern that only allows integer input
        Pattern EditintegerPattern = Pattern.compile("-?\\d*");
        TextFormatter<String> EditIntegerFormatter = new TextFormatter<>(change -> {
            if (EditintegerPattern.matcher(change.getControlNewText()).matches()) {
                return change;
            } else {
                return null;
            }
        });

        // Set the formatter to the productAdd_quantity TextField
        editProduct_quantity.setTextFormatter(EditIntegerFormatter);

        //Price REGEX
        Pattern EditintegerPrice = Pattern.compile("-?\\d*");
        TextFormatter<String> EditIntegerPrice = new TextFormatter<>(change -> {
            if (EditintegerPrice.matcher(change.getControlNewText()).matches()) {
                return change;
            } else {
                return null;
            }
        });
        editProduct_price.setTextFormatter(EditIntegerPrice);
    }

    @FXML
    void editProduct_confirm_handle(ActionEvent event) {
        if (validateInputs()) {
            if (updateProductInDatabase()) {
                if (productListController != null) {
                    productListController.refreshProductList();
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully.");
                closeStage();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid input for all fields.");
        }
    }

    @FXML
    void editProduct_cancel_handle(ActionEvent event) {
        closeStage();
    }

    private boolean validateInputs() {
        try {
            Double.parseDouble(editProduct_price.getText());
            Integer.parseInt(editProduct_quantity.getText());
            return !editProduct_name.getText().isEmpty() &&
                    !editProduct_quality.getText().isEmpty();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean updateProductInDatabase() {
        String sql = "UPDATE product_list SET product_name = ?, price = ?, quantity = ?, quality = ? WHERE id_product = ?";
        try (Connection conn = DatabaseCon.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, editProduct_name.getText());
            pstmt.setDouble(2, Double.parseDouble(editProduct_price.getText()));
            pstmt.setInt(3, Integer.parseInt(editProduct_quantity.getText()));
            pstmt.setString(4, editProduct_quality.getText());
            pstmt.setInt(5, currentProduct.getIdProduct());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating the product: " + e.getMessage());
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeStage() {
        Stage stage = (Stage) editProduct_confirmBtn.getScene().getWindow();
        stage.close();
    }
}
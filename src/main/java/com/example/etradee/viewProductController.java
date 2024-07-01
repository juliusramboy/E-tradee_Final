package com.example.etradee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class viewProductController {
    @FXML
    private Button viewProduct_backBtn;
    @FXML
    private Button viewProduct_cancelBtn;
    @FXML
    private Button viewProduct_editBtn;
    @FXML
    private Button viewProduct_deleteBtn;
    @FXML
    private TextField viewProduct_name;
    @FXML
    private TextField viewProduct_price;
    @FXML
    private TextField viewProduct_quality;
    @FXML
    private TextField viewProduct_quantity;

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
            viewProduct_name.setText(currentProduct.getProductName());
            viewProduct_price.setText(String.valueOf(currentProduct.getPrice()));
            viewProduct_quality.setText(currentProduct.getQuality());
            viewProduct_quantity.setText(String.valueOf(currentProduct.getQuantity()));
        }
    }

    @FXML
    void viewProduct_backHandle(ActionEvent event) {
        closeStage();
    }

    @FXML
    void viewProduct_deletelHandle(ActionEvent event) {
        if (confirmDelete()) {
            if (deleteProductFromDatabase()) {
                if (productListController != null) {
                    productListController.refreshProductList();
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully.");
                closeStage();
            }
        }
    }

    @FXML
    void viewProduct_editHandle(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editproductPanel.fxml"));
            Parent root = loader.load();

            editProductController controller = loader.getController();
            controller.setProductListController(productListController);
            controller.setProduct(currentProduct);

            Stage stage = new Stage();
            stage.setTitle("Edit Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(viewProduct_editBtn.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.showAndWait();

            // After the edit panel is closed, refresh the current view
            if (productListController != null) {
                productListController.refreshProductList();
                // Refresh the current product data
                setProduct(productListController.getProductById(currentProduct.getIdProduct()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean confirmDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Product");
        alert.setContentText("Are you sure you want to delete this product?");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private boolean deleteProductFromDatabase() {
        String sql = "DELETE FROM product_list WHERE id_product = ?";

        try (Connection conn = DatabaseCon.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentProduct.getIdProduct());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting the product: " + e.getMessage());
            return false;
        }
    }

    private boolean updateProductInDatabase() {
        String sql = "UPDATE product_list SET product_name = ?, price = ?, quantity = ?, quality = ? WHERE id_product = ?";
        try (Connection conn = DatabaseCon.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, viewProduct_name.getText());
            pstmt.setDouble(2, Double.parseDouble(viewProduct_price.getText()));
            pstmt.setInt(3, Integer.parseInt(viewProduct_quantity.getText()));
            pstmt.setString(4, viewProduct_quality.getText());
            pstmt.setInt(5, currentProduct.getIdProduct());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully.");
                return true;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product.");
                return false;
            }
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
        Stage stage = (Stage) viewProduct_backBtn.getScene().getWindow();
        stage.close();
    }
}
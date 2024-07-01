package com.example.etradee;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class addProductController {

    @FXML
    private Button productAdd_cancelBtn;
    @FXML
    private Button productAdd_confirmBtn;
    @FXML
    private TextField productAdd_name;
    @FXML
    private TextField productAdd_price;
    @FXML
    private TextField productAdd_quality;
    @FXML
    private TextField productAdd_quantity;

    private productListCrtl productListController;


    public void setProductListController(productListCrtl controller) {
        this.productListController = controller;
    }
    @FXML
    public void initialize() {
        //Quantity REGEX
        // Create a pattern that only allows integer input
        Pattern integerPattern = Pattern.compile("-?\\d*");
        TextFormatter<String> integerFormatter = new TextFormatter<>(change -> {
            if (integerPattern.matcher(change.getControlNewText()).matches()) {
                return change;
            } else {
                return null;
            }
        });

        // Set the formatter to the productAdd_quantity TextField
        productAdd_quantity.setTextFormatter(integerFormatter);

        //Price REGEX
        Pattern integerPrice = Pattern.compile("-?\\d*");
        TextFormatter<String> integerPriceFormattter = new TextFormatter<>(change -> {
            if (integerPrice.matcher(change.getControlNewText()).matches()) {
                return change;
            } else {
                return null;
            }
        });
        productAdd_price.setTextFormatter(integerPriceFormattter);
    }

    @FXML
    void productAdd_cancelHandle(ActionEvent event) {
        closeStage();
    }

    @FXML
    void productAdd_confirmHandle(ActionEvent event) {
        if (validateInputs()) {
            addProductToDatabase();
            productListController.refreshProductList();
            closeStage();
        }
    }


    private boolean validateInputs() {
        // Add your validation logic here
        return !productAdd_name.getText().isEmpty() &&
                !productAdd_price.getText().isEmpty() &&
                !productAdd_quality.getText().isEmpty() &&
                !productAdd_quantity.getText().isEmpty();
    }

    private boolean addProductToDatabase() {
        String sql = "INSERT INTO product_list (product_name, price, quantity, quality) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseCon.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productAdd_name.getText());
            pstmt.setDouble(2, Double.parseDouble(productAdd_price.getText()));
            pstmt.setInt(3, Integer.parseInt(productAdd_quantity.getText()));
            pstmt.setString(4, productAdd_quality.getText());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message)
            return false;
        }
    }


    private void closeStage() {
        Stage stage = (Stage) productAdd_cancelBtn.getScene().getWindow();
        stage.close();
    }
}
package com.example.etradee;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.print.PrinterJob;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptController {

    @FXML
    private Label transactionNumberText;

    @FXML
    private Text accountNameText;

    @FXML
    private Label dateText;

    @FXML
    private Label timeText;

    @FXML
    private VBox itemsContainer;

    @FXML
    private Label totalText;

    @FXML
    private Label changeText;

    @FXML
    private VBox itemsVBox;

    private Stage previewStage;

    public void setPreviewStage(Stage stage) {
        this.previewStage = stage;
    }

    public void setReceiptData(String transactionNumber, String accountName, LocalDate date, LocalTime time, List<Product> items, double total, double change) {
        transactionNumberText.setText(transactionNumber);
        accountNameText.setText(accountName);
        dateText.setText(date.toString());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        timeText.setText(time.format(formatter));

        itemsContainer.getChildren().clear();
        for (Product item : items) {
            Text nameLabel = new Text();
            nameLabel.setText(item.getName());
            nameLabel.setWrappingWidth(133.33); // Set the width for the name label
            nameLabel.setFont(Font.font(15)); // Set the font size to 15

            Text quantityLabel = new Text();
            quantityLabel.setText("" + item.getQuantity());
            quantityLabel.setWrappingWidth(133.33); // Set the width for the quantity label
            quantityLabel.setTextAlignment(TextAlignment.CENTER);
            quantityLabel.setFont(Font.font(15)); // Set the font size to 15

            Text priceLabel = new Text();
            priceLabel.setText("" + item.getPrice());
            priceLabel.setWrappingWidth(133.33); // Set the width for the price label
            priceLabel.setTextAlignment(TextAlignment.CENTER);
            priceLabel.setFont(Font.font(15)); // Set the font size to 15

            // Create an HBox for each item to hold its details
            HBox itemBox = new HBox(); // 10 is the spacing between elements
            itemBox.setPrefSize(400, 30); // Adjust the preferred size as needed
            itemBox.setAlignment(Pos.CENTER_LEFT); // Align items to the left

            // Add padding to the nameLabel
            HBox.setMargin(nameLabel, new Insets(0, 0, 0, 15));

            // Add the text elements to the HBox
            itemBox.getChildren().addAll(nameLabel, quantityLabel, priceLabel);

            // Add the HBox for this item to the itemsContainer
            itemsContainer.getChildren().add(itemBox);
        }

        totalText.setText(String.format("₱%.2f", total));
        changeText.setText(String.format("₱%.2f", change));

    }

        @FXML
    private void handlePrint() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(previewStage)) {
            boolean success = printerJob.printPage(itemsVBox);
            if (success) {
                printerJob.endJob();
            }
        }
    }
}

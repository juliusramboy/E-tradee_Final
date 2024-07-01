package com.example.etradee;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class admin_addController implements Initializable {

    @FXML
    private TableView<User> usersTableView;

    @FXML
    private TableColumn<User, String> ID_Column;

    @FXML
    private TableColumn<User, String> Name_Column;

    @FXML
    private TableColumn<User, String> Password_Column;

    @FXML
    private TableColumn<User, String> Contact_Column;

    @FXML
    private TableColumn<User, String> Address_Column;

    @FXML
    private TextField searchField;

    @FXML
    private Button logoutButtonAdmin;

    @FXML
    private Button addEmployee;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<User> filteredUserList = FXCollections.observableArrayList();
    private Stage login;

    private Stage primaryStage; // Reference to the primary stage


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ID_Column.setCellValueFactory(new PropertyValueFactory<>("id"));
        Name_Column.setCellValueFactory(new PropertyValueFactory<>("employee_name"));
        Password_Column.setCellValueFactory(new PropertyValueFactory<>("password"));
        Contact_Column.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        Address_Column.setCellValueFactory(new PropertyValueFactory<>("address"));

        usersTableView.setItems(userList);
        loadUserData();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterUserList(newValue));

        usersTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    showUserDetails(selectedUser);
                }
            }
        });
    }

    private void loadUserData() {
        userList.clear();
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, username, employee_name, password, role, address, contactNum, profile_picture_path FROM users")) {

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String role = resultSet.getString("role");
                String employee_name = resultSet.getString("employee_name");
                String address = resultSet.getString("address");
                String contactNum = resultSet.getString("contactNum");
                byte[] profilePictureData = resultSet.getBytes("profile_picture_path");

                User user = new User(id, employee_name, password, contactNum, address, role, username);
                user.setProfilePictureData(profilePictureData);

                userList.add(user);
            }

            filteredUserList.setAll(userList);
            usersTableView.setItems(filteredUserList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refreshTable() {
        loadUserData();
    }

    private void filterUserList(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredUserList.setAll(userList);
        } else {
            filteredUserList.clear();
            String lowerCaseFilter = searchText.toLowerCase();
            for (User user : userList) {
                if (user.getEmployee_name().toLowerCase().contains(lowerCaseFilter)) {
                    filteredUserList.add(user);
                }
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DatabaseCon.getConnection();
    }

    public static class User {
        private String id;
        private String employee_name;
        private String password;
        private String contactNumber;
        private String address;
        private String role;
        private String username;
        private byte[] profilePictureData;

        public User(String id, String employee_name, String password, String contactNumber, String address, String role, String username) {
            this.id = id;
            this.employee_name = employee_name;
            this.password = password;
            this.contactNumber = contactNumber;
            this.address = address;
            this.role = role;
            this.username = username;
        }

        // Getters and setters

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmployee_name() { return employee_name; }
        public void setEmployee_name(String employee_name) { this.employee_name = employee_name; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getContactNumber() { return contactNumber; }
        public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public byte[] getProfilePictureData() { return profilePictureData; }
        public void setProfilePictureData(byte[] data) { this.profilePictureData = data; }
    }

    @FXML
    private void add_employee(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add_employeeAdmin.fxml"));
            Parent root = loader.load();

            addEmployee controller = loader.getController();
            try {
                controller.setConnection(DatabaseCon.getConnection());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            Stage stage = new Stage();
            stage.setTitle("Add Employee");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.show();

            controller.setAdminController(this);
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }
    //Show user
    private void showUserDetails(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("viewPanel.fxml"));
            Parent root = loader.load();

            viewPanelController controller = loader.getController();
            controller.setUserDetails(user);
            controller.setAdminController(this);

            Stage stage = new Stage();
            stage.setTitle("View employee");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogoutAdmin() {
        Stage stage = (Stage) logoutButtonAdmin.getScene().getWindow(); // Use any UI component to get the current stage
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

    //Change scene
    //Employeelist
    @FXML
    void ADMIN_Employeelist_handle(ActionEvent event) {

    }

    //Product List
    @FXML
    void ADMIN_Productlist_handle(ActionEvent event) {
        Stage stage = (Stage) logoutButtonAdmin.getScene().getWindow(); // Use any UI component to get the current stage
        stage.close(); // Close the current stage (admin_add window)

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("productListpanel.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Product list");
            newStage.setScene(new Scene(root));
            newStage.initStyle(StageStyle.UTILITY);
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Sales
    @FXML
    void ADMIN_Sales_handle(ActionEvent event) {
        Stage stage = (Stage) logoutButtonAdmin.getScene().getWindow(); // Use any UI component to get the current stage
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
}

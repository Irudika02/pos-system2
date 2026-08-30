package com.devstack.pos.controller;

import com.devstack.pos.bo.BOFactory;
import com.devstack.pos.bo.custom.CustomerBO;
import com.devstack.pos.dto.CustomerDTO;
import com.devstack.pos.util.BoType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CustomerManagementFormController {
    public AnchorPane context;
    public TextField txtId;
    public TextField txtName;
    public TextField txtAddress;
    public TextField txtSalary;
    public TextField txtSearch;
    public Button btnSave;
    public TableView<CustomerDTO> tblCustomer;
    public TableColumn<CustomerDTO, String> colId;
    public TableColumn<CustomerDTO, String> colName;
    public TableColumn<CustomerDTO, String> colAddress;
    public TableColumn<CustomerDTO, Double> colSalary;

    private CustomerBO customerBO = BOFactory.getInstance().getBo(BoType.CUSTOMER);

    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));

        loadAllCustomers();
        generateNewId();

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                searchCustomers(newValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        tblCustomer.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                txtId.setText(newValue.getCustomerId());
                txtName.setText(newValue.getName());
                txtAddress.setText(newValue.getAddress());
                txtSalary.setText(String.valueOf(newValue.getSalary()));
                btnSave.setText("Update Customer");
            }
        });
    }

    private void generateNewId() {
        txtId.setText("C-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
    }

    private void loadAllCustomers() {
        try {
            List<CustomerDTO> allCustomers = customerBO.getAllCustomers();
            ObservableList<CustomerDTO> obList = FXCollections.observableArrayList(allCustomers);
            tblCustomer.setItems(obList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchCustomers(String text) throws SQLException, ClassNotFoundException {
        List<CustomerDTO> list = customerBO.searchByName(text);
        tblCustomer.setItems(FXCollections.observableArrayList(list));
    }

    public void NewCustomerOnAction(ActionEvent actionEvent) {
        clearFields();
        generateNewId();
        btnSave.setText("Save Customer");
    }

    public void SaveCustomerOnAction(ActionEvent actionEvent) {
        try {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String address = txtAddress.getText().trim();
            double salary = Double.parseDouble(txtSalary.getText().trim());

            CustomerDTO dto = new CustomerDTO(id, name, address, salary);

            if (btnSave.getText().equalsIgnoreCase("Save Customer")) {
                boolean saved = customerBO.saveCustomer(dto);
                if (saved) {
                    new Alert(Alert.AlertType.INFORMATION, "Customer Saved Successfully!").show();
                    loadAllCustomers();
                    NewCustomerOnAction(null);
                } else {
                    new Alert(Alert.AlertType.WARNING, "Failed to Save Customer").show();
                }
            } else {
                boolean updated = customerBO.updateCustomer(dto);
                if (updated) {
                    new Alert(Alert.AlertType.INFORMATION, "Customer Updated Successfully!").show();
                    loadAllCustomers();
                    NewCustomerOnAction(null);
                } else {
                    new Alert(Alert.AlertType.WARNING, "Failed to Update Customer").show();
                }
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
        }
    }

    public void BackToDashboardOnAction(ActionEvent actionEvent) throws IOException {
        URL resource = getClass().getResource("/com/devstack/pos/view/DashboardForm.fxml");
        Parent load = FXMLLoader.load(resource);
        Stage stage = (Stage) context.getScene().getWindow();
        stage.setScene(new Scene(load));
    }

    private void clearFields() {
        txtName.clear();
        txtAddress.clear();
        txtSalary.clear();
        tblCustomer.getSelectionModel().clearSelection();
    }
}

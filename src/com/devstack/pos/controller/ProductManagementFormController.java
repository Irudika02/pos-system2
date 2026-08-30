package com.devstack.pos.controller;

import com.devstack.pos.bo.BOFactory;
import com.devstack.pos.bo.custom.ProductBO;
import com.devstack.pos.dto.ProductDTO;
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

public class ProductManagementFormController {
    public AnchorPane context;
    public TextField txtCode;
    public TextField txtDescription;
    public TextField txtUnitPrice;
    public TextField txtQty;
    public TextField txtSearch;
    public Button btnSave;
    public TableView<ProductDTO> tblProduct;
    public TableColumn<ProductDTO, String> colCode;
    public TableColumn<ProductDTO, String> colDescription;
    public TableColumn<ProductDTO, Double> colUnitPrice;
    public TableColumn<ProductDTO, Integer> colQty;
    public TableColumn<ProductDTO, String> colQr;

    private ProductBO productBO = BOFactory.getInstance().getBo(BoType.PRODUCT);

    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        colQr.setCellValueFactory(new PropertyValueFactory<>("qrCode"));

        loadAllProducts();
        generateNewCode();

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                searchProducts(newValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        tblProduct.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                txtCode.setText(newValue.getCode());
                txtDescription.setText(newValue.getDescription());
                txtUnitPrice.setText(String.valueOf(newValue.getUnitPrice()));
                txtQty.setText(String.valueOf(newValue.getQtyOnHand()));
                btnSave.setText("Update Product");
            }
        });
    }

    private void generateNewCode() {
        txtCode.setText("P-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
    }

    private void loadAllProducts() {
        try {
            List<ProductDTO> allProducts = productBO.getAllProducts();
            ObservableList<ProductDTO> obList = FXCollections.observableArrayList(allProducts);
            tblProduct.setItems(obList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchProducts(String text) throws SQLException, ClassNotFoundException {
        List<ProductDTO> list = productBO.searchByDescription(text);
        tblProduct.setItems(FXCollections.observableArrayList(list));
    }

    public void NewProductOnAction(ActionEvent actionEvent) {
        clearFields();
        generateNewCode();
        btnSave.setText("Save Product");
    }

    public void SaveCustomerOnAction(ActionEvent actionEvent) {
        SaveProductOnAction(actionEvent);
    }

    public void SaveProductOnAction(ActionEvent actionEvent) {
        try {
            String code = txtCode.getText().trim();
            String description = txtDescription.getText().trim();
            double unitPrice = Double.parseDouble(txtUnitPrice.getText().trim());
            int qty = Integer.parseInt(txtQty.getText().trim());
            String qr = "QR-" + code;

            ProductDTO dto = new ProductDTO(code, description, unitPrice, qty, qr);

            if (btnSave.getText().equalsIgnoreCase("Save Product")) {
                boolean saved = productBO.saveProduct(dto);
                if (saved) {
                    new Alert(Alert.AlertType.INFORMATION, "Product Saved Successfully!").show();
                    loadAllProducts();
                    NewProductOnAction(null);
                } else {
                    new Alert(Alert.AlertType.WARNING, "Failed to Save Product").show();
                }
            } else {
                boolean updated = productBO.updateProduct(dto);
                if (updated) {
                    new Alert(Alert.AlertType.INFORMATION, "Product Updated Successfully!").show();
                    loadAllProducts();
                    NewProductOnAction(null);
                } else {
                    new Alert(Alert.AlertType.WARNING, "Failed to Update Product").show();
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
        txtDescription.clear();
        txtUnitPrice.clear();
        txtQty.clear();
        tblProduct.getSelectionModel().clearSelection();
    }
}

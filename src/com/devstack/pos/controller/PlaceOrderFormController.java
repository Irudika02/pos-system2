package com.devstack.pos.controller;

import com.devstack.pos.bo.BOFactory;
import com.devstack.pos.bo.custom.CustomerBO;
import com.devstack.pos.bo.custom.OrderBO;
import com.devstack.pos.bo.custom.ProductBO;
import com.devstack.pos.dto.CartItemDTO;
import com.devstack.pos.dto.CustomerDTO;
import com.devstack.pos.dto.OrderDTO;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlaceOrderFormController {
    public AnchorPane context;
    public Label lblOrderId;
    public TextField txtCustomerId;
    public TextField txtCustomerName;
    public TextField txtCustomerAddress;
    public TextField txtCustomerSalary;
    public TextField txtProductCode;
    public TextField txtProductDescription;
    public TextField txtUnitPrice;
    public TextField txtQtyOnHand;
    public TextField txtQty;
    public Label lblTotal;

    public TableView<CartItemDTO> tblCart;
    public TableColumn<CartItemDTO, String> colCode;
    public TableColumn<CartItemDTO, String> colDescription;
    public TableColumn<CartItemDTO, Double> colUnitPrice;
    public TableColumn<CartItemDTO, Integer> colQty;
    public TableColumn<CartItemDTO, Double> colTotal;

    private CustomerBO customerBO = BOFactory.getInstance().getBo(BoType.CUSTOMER);
    private ProductBO productBO = BOFactory.getInstance().getBo(BoType.PRODUCT);
    private OrderBO orderBO = BOFactory.getInstance().getBo(BoType.ORDER);

    private ObservableList<CartItemDTO> cartList = FXCollections.observableArrayList();

    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        tblCart.setItems(cartList);
        generateOrderId();

        // Customer ID listener
        txtCustomerId.setOnAction(event -> searchCustomer());
        // Product Code listener
        txtProductCode.setOnAction(event -> searchProduct());
    }

    private void generateOrderId() {
        try {
            String nextId = orderBO.generateNextOrderId();
            if (lblOrderId != null) {
                lblOrderId.setText("Place Order (Order ID: " + nextId + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchCustomer() {
        try {
            CustomerDTO c = customerBO.searchCustomer(txtCustomerId.getText().trim());
            if (c != null) {
                txtCustomerName.setText(c.getName());
                txtCustomerAddress.setText(c.getAddress());
                txtCustomerSalary.setText(String.valueOf(c.getSalary()));
                txtProductCode.requestFocus();
            } else {
                new Alert(Alert.AlertType.WARNING, "Customer Not Found!").show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchProduct() {
        try {
            ProductDTO p = productBO.searchProduct(txtProductCode.getText().trim());
            if (p != null) {
                txtProductDescription.setText(p.getDescription());
                txtUnitPrice.setText(String.valueOf(p.getUnitPrice()));
                txtQtyOnHand.setText(String.valueOf(p.getQtyOnHand()));
                txtQty.requestFocus();
            } else {
                new Alert(Alert.AlertType.WARNING, "Product Not Found!").show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void NewOrderOnAction(ActionEvent actionEvent) {
        cartList.clear();
        calculateTotal();
        generateOrderId();
        txtCustomerId.clear();
        txtCustomerName.clear();
        txtCustomerAddress.clear();
        txtCustomerSalary.clear();
        txtProductCode.clear();
        txtProductDescription.clear();
        txtUnitPrice.clear();
        txtQtyOnHand.clear();
        txtQty.clear();
    }

    public void AddToCartOnAction(ActionEvent actionEvent) {
        try {
            String code = txtProductCode.getText().trim();
            String description = txtProductDescription.getText().trim();
            double unitPrice = Double.parseDouble(txtUnitPrice.getText().trim());
            int qtyOnHand = Integer.parseInt(txtQtyOnHand.getText().trim());
            int qty = Integer.parseInt(txtQty.getText().trim());

            if (qty <= 0 || qty > qtyOnHand) {
                new Alert(Alert.AlertType.WARNING, "Invalid Quantity! Available: " + qtyOnHand).show();
                return;
            }

            double total = unitPrice * qty;

            for (CartItemDTO item : cartList) {
                if (item.getCode().equals(code)) {
                    item.setQty(item.getQty() + qty);
                    item.setTotal(item.getUnitPrice() * item.getQty());
                    tblCart.refresh();
                    calculateTotal();
                    clearProductFields();
                    return;
                }
            }

            cartList.add(new CartItemDTO(code, description, unitPrice, qty, total));
            calculateTotal();
            clearProductFields();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Please enter valid fields!").show();
        }
    }

    public void QTYOnAction(ActionEvent actionEvent) {
        AddToCartOnAction(actionEvent);
    }

    public void PlaceOrderOnAction(ActionEvent actionEvent) {
        if (cartList.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Cart is empty! Please add items first.").show();
            return;
        }

        try {
            String orderId = lblOrderId.getText().replaceAll("[^A-Za-z0-9-]", "").replace("PlaceOrderOrderID", "").trim();
            if (orderId.isEmpty()) orderId = orderBO.generateNextOrderId();
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            double totalCost = Double.parseDouble(lblTotal.getText().replace("Total: LKR ", "").replace("Total: ", "").trim());
            String customerId = txtCustomerId.getText().trim();

            List<CartItemDTO> items = new ArrayList<>(cartList);
            OrderDTO orderDTO = new OrderDTO(orderId, date, totalCost, customerId, "admin@pos.com", items);

            boolean isPlaced = orderBO.placeOrder(orderDTO);
            if (isPlaced) {
                new Alert(Alert.AlertType.INFORMATION, "Order Placed Successfully! Order ID: " + orderId).show();
                NewOrderOnAction(null);
            } else {
                new Alert(Alert.AlertType.WARNING, "Failed to Place Order!").show();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error placing order: " + e.getMessage()).show();
            e.printStackTrace();
        }
    }

    public void BackToDashboardOnAction(ActionEvent actionEvent) throws IOException {
        URL resource = getClass().getResource("/com/devstack/pos/view/DashboardForm.fxml");
        Parent load = FXMLLoader.load(resource);
        Stage stage = (Stage) context.getScene().getWindow();
        stage.setScene(new Scene(load));
    }

    private void calculateTotal() {
        double total = 0;
        for (CartItemDTO item : cartList) {
            total += item.getTotal();
        }
        lblTotal.setText("Total: LKR " + String.format("%.2f", total));
    }

    private void clearProductFields() {
        txtProductCode.clear();
        txtProductDescription.clear();
        txtUnitPrice.clear();
        txtQtyOnHand.clear();
        txtQty.clear();
        txtProductCode.requestFocus();
    }
}

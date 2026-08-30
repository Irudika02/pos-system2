package com.devstack.pos.controller;

import com.devstack.pos.bo.BOFactory;
import com.devstack.pos.bo.custom.OrderBO;
import com.devstack.pos.dto.OrderDTO;
import com.devstack.pos.util.BoType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class OrderHistoryFormController {
    public AnchorPane context;
    public TextField txtSearch;
    public TableView<OrderDTO> tblOrders;
    public TableColumn<OrderDTO, String> colId;
    public TableColumn<OrderDTO, String> colCustomer;
    public TableColumn<OrderDTO, Double> colTotal;
    public TableColumn<OrderDTO, String> colDate;
    public TableColumn<OrderDTO, String> colUser;

    private OrderBO orderBO = BOFactory.getInstance().getBo(BoType.ORDER);

    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userEmail"));

        loadAllOrders();
    }

    private void loadAllOrders() {
        try {
            List<OrderDTO> allOrders = orderBO.getAllOrders();
            ObservableList<OrderDTO> obList = FXCollections.observableArrayList(allOrders);
            tblOrders.setItems(obList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void BackToDashboardOnAction(ActionEvent actionEvent) throws IOException {
        URL resource = getClass().getResource("/com/devstack/pos/view/DashboardForm.fxml");
        Parent load = FXMLLoader.load(resource);
        Stage stage = (Stage) context.getScene().getWindow();
        stage.setScene(new Scene(load));
    }
}

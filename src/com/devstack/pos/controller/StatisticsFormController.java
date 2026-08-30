package com.devstack.pos.controller;

import com.devstack.pos.bo.BOFactory;
import com.devstack.pos.bo.custom.CustomerBO;
import com.devstack.pos.bo.custom.OrderBO;
import com.devstack.pos.bo.custom.ProductBO;
import com.devstack.pos.dto.OrderDTO;
import com.devstack.pos.util.BoType;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class StatisticsFormController {
    public AnchorPane context;
    public AreaChart<String, Number> chartRevenue;
    public Label lblTotalCustomers;
    public Label lblTotalProducts;
    public Label lblTotalOrders;
    public Label lblTotalRevenue;

    private CustomerBO customerBO = BOFactory.getInstance().getBo(BoType.CUSTOMER);
    private ProductBO productBO = BOFactory.getInstance().getBo(BoType.PRODUCT);
    private OrderBO orderBO = BOFactory.getInstance().getBo(BoType.ORDER);

    public void initialize() {
        LoadDataOnAction(null);
    }

    public void LoadDataOnAction(ActionEvent actionEvent) {
        try {
            int customers = customerBO.getAllCustomers().size();
            int products = productBO.getAllProducts().size();
            List<OrderDTO> orders = orderBO.getAllOrders();
            int orderCount = orders.size();
            double totalRev = 0;

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue Over Orders");

            for (int i = orders.size() - 1; i >= 0; i--) {
                OrderDTO o = orders.get(i);
                totalRev += o.getTotalCost();
                if (chartRevenue != null) {
                    series.getData().add(new XYChart.Data<>(o.getOrderId(), o.getTotalCost()));
                }
            }

            if (chartRevenue != null) {
                chartRevenue.getData().clear();
                chartRevenue.getData().add(series);
            }

            if (lblTotalCustomers != null) lblTotalCustomers.setText(String.valueOf(customers));
            if (lblTotalProducts != null) lblTotalProducts.setText(String.valueOf(products));
            if (lblTotalOrders != null) lblTotalOrders.setText(String.valueOf(orderCount));
            if (lblTotalRevenue != null) lblTotalRevenue.setText("LKR " + String.format("%.2f", totalRev));
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

package com.devstack.pos.controller;

import com.devstack.pos.env.StaticResource;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DashboardFormController {
    public AnchorPane context;
    public Label lblCompany;
    public Label lblVersion;
    public Label lblDate;
    public Label lblTime;

    public void initialize() {
        setStaticdata();
        setDateAndTime();
    }

    private void setDateAndTime() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), actionEvent -> {
            LocalDateTime now = LocalDateTime.now();
            if (lblDate != null) lblDate.setText(now.format(dateFormatter));
            if (lblTime != null) lblTime.setText(now.format(timeFormatter));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void setStaticdata() {
        if (lblVersion != null) lblVersion.setText("Version: " + StaticResource.getVERSION());
        if (lblCompany != null) lblCompany.setText("From: " + StaticResource.getCOMPANY());
    }

    public void LogoutOnAction(ActionEvent actionEvent) throws IOException {
        setUi("MainForm");
    }

    public void OpenCustomerManagementOnAction(MouseEvent event) throws IOException {
        setUi("CustomerManagementForm");
    }

    public void OpenProductManagementOnAction(MouseEvent event) throws IOException {
        setUi("ProductManagementform");
    }

    public void OpenPlaceOrderOnAction(MouseEvent event) throws IOException {
        setUi("PlaceOrderForm");
    }

    public void OpenOrderHistoryOnAction(MouseEvent event) throws IOException {
        setUi("OrderHistoryForm");
    }

    public void OpenStatisticsOnAction(MouseEvent event) throws IOException {
        setUi("StatisticsForm");
    }

    public void OpenBackupsOnAction(MouseEvent event) throws IOException {
        setUi("BackupForm");
    }

    private void setUi(String location) throws IOException {
        URL resource = getClass().getResource("/com/devstack/pos/view/" + location + ".fxml");
        Parent load = FXMLLoader.load(resource);
        Stage stage = (Stage) context.getScene().getWindow();
        stage.setScene(new Scene(load));
    }
}

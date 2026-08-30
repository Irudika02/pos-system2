package com.devstack.pos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class BackupFormController {
    public AnchorPane context;

    public void BackToDashboardOnAction(ActionEvent actionEvent) throws IOException {
        URL resource = getClass().getResource("/com/devstack/pos/view/DashboardForm.fxml");
        Parent load = FXMLLoader.load(resource);
        Stage stage = (Stage) context.getScene().getWindow();
        stage.setScene(new Scene(load));
    }

    public void CreateBackupOnAction(ActionEvent actionEvent) {
        new Alert(Alert.AlertType.INFORMATION, "Database Backup Created Successfully!").show();
    }
}

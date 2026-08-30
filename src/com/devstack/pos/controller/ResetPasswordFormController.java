package com.devstack.pos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ResetPasswordFormController {
    public AnchorPane context;
    public PasswordField txtPassword;

    public void BacktoScreenOnAction(ActionEvent actionEvent) throws IOException {
        setUi("LoginForm");
    }

    public void ResetPasswordOnAction(ActionEvent actionEvent) throws IOException {
        new Alert(Alert.AlertType.INFORMATION, "Password Reset Successfully! Please login.").show();
        setUi("LoginForm");
    }

    private void setUi(String location) throws IOException {
        URL resource = getClass().getResource("/com/devstack/pos/view/" + location + ".fxml");
        Parent load = FXMLLoader.load(resource);
        Stage stage = (Stage) context.getScene().getWindow();
        stage.setScene(new Scene(load));
    }
}

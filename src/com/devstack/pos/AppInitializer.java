package com.devstack.pos;

import com.devstack.pos.server.MobilePosServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        // Start Mobile POS Web Server for iPhone access on port 8080
        MobilePosServer.startServer();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL resource = getClass().getResource("/com/devstack/pos/view/MainForm.fxml");
        Parent load = FXMLLoader.load(resource);
        Scene scene = new Scene(load);
        primaryStage.setScene(scene);
        primaryStage.setTitle("DS POS SYSTEM - Desktop & iPhone Server");
        primaryStage.show();
    }
}

module POS.SYSTEM {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires jbcrypt;
    requires jdk.httpserver;

    opens com.devstack.pos to javafx.fxml;
    opens com.devstack.pos.controller to javafx.fxml;
    opens com.devstack.pos.dto to javafx.base;
    opens com.devstack.pos.model to javafx.base;
    opens com.devstack.pos.entity to javafx.base;

    exports com.devstack.pos;
}
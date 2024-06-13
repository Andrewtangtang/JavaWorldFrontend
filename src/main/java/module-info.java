module com.javaworld.client {
    requires com.almasb.fxgl.all;
    requires javafx.web;

    requires com.javaworld;
    requires com.javaworld.adapter;

    opens com.javaworld.clientapp to javafx.fxml;
    exports com.javaworld.clientapp;
}
open module com.javaworld.clientapp {
    requires com.almasb.fxgl.all;
    requires javafx.web;
    requires jdk.crypto.cryptoki;

    requires com.javaworld;
    requires com.javaworld.adapter;

    exports com.javaworld.clientapp;
}
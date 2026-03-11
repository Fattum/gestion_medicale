module com.example.gestion_medicale {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.slf4j;
    requires org.slf4j.simple;

    opens com.example.gestion_medicale to javafx.fxml;
    opens com.example.gestion_medicale.controllers to javafx.fxml;
    opens com.example.gestion_medicale.models to javafx.base, javafx.fxml;

    exports com.example.gestion_medicale;
    exports com.example.gestion_medicale.controllers;
    exports com.example.gestion_medicale.models;
}
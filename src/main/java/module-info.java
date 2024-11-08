module com.thetechnoobs.dupdefender {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires eu.hansolo.tilesfx;
    requires juniversalchardet;
    requires java.desktop;
    requires javafx.media;
    requires javafx.swing;
    requires java.compiler;

    opens com.thetechnoobs.dupdefender to javafx.fxml;
    exports com.thetechnoobs.dupdefender;
    exports com.thetechnoobs.dupdefender.models;
}
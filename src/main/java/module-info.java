module com.thetechnoobs.dupdefender {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires eu.hansolo.tilesfx;

    opens com.thetechnoobs.dupdefender to javafx.fxml;
    exports com.thetechnoobs.dupdefender;
}
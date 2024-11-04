module com.thetechnoobs.dupdefender {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires eu.hansolo.tilesfx;
    requires org.json;
    requires ini4j;
    requires org.jsoup;
    requires juniversalchardet;
    requires java.desktop;
    requires javafx.media;
    requires javafx.swing;
    requires jcodec.javase;

    opens com.thetechnoobs.dupdefender to javafx.fxml;
    exports com.thetechnoobs.dupdefender;
    exports com.thetechnoobs.dupdefender.models;
}
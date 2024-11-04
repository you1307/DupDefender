package com.thetechnoobs.fishtank.dupdestroyer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainEntry extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainEntry.class.getResource("main-screen.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("DupDestroyer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
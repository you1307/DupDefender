package com.thetechnoobs.dupdefender.controllers;

import com.thetechnoobs.dupdefender.models.SongModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class FindDupsLoadingScreenController {

    private ArrayList<SongModel> songModels = new ArrayList<>();
    private ArrayList<ArrayList<SongModel>> duplicatsList = new ArrayList<>();
    public Label loadingProgress;

    private double progress = 0;

    public void initialize(){


    }

    private void searchForDups(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < songModels.size(); i++) {
                    progress++;
                    //the model that will be used to compair the rest
                    SongModel referanceSongModel = songModels.get(i);

                    ArrayList<SongModel> possibleSameCharts = new ArrayList<>();
                    possibleSameCharts.add(referanceSongModel);

                    for (int j = 0; j < songModels.size(); j++) {
                        boolean sameName = referanceSongModel.name.equals(songModels.get(j).name);
                        boolean sameArtist = referanceSongModel.artist.equals(songModels.get(j).artist);
                        boolean sameFilePath = referanceSongModel.chartFolderPath.equals(songModels.get(j).chartFolderPath);

                        if(sameArtist && sameName && !sameFilePath){
                            possibleSameCharts.add(songModels.get(j));
                        }
                    }
                    if(possibleSameCharts.size() > 1){
                        duplicatsList.add(possibleSameCharts);
                    }


                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadingProgress.setText(""+duplicatsList.size());
                        openDupWindow();
                        closeThisWindow();
                    }
                });
            }
        });

        thread.start();
    }

    public void closeThisWindow() {
        Stage stage = (Stage) loadingProgress.getScene().getWindow();  // Replace loadingProgress with any Node in the scene
        stage.close();  // Close the current window
    }


    private void openDupWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("duplicate_chart_manager.fxml"));
            Parent root = fxmlLoader.load();

            Stage newWindow = new Stage();
            newWindow.setTitle("DupDefender");
            newWindow.setScene(new Scene(root));
            DuplicateChartManagerController duplicateChartManagerController = fxmlLoader.getController();

            ObservableList<ArrayList<SongModel>> observableDupList = FXCollections.observableArrayList();
            observableDupList.addAll(duplicatsList);
            duplicateChartManagerController.setDupListModels(observableDupList);

            newWindow.initModality(Modality.APPLICATION_MODAL);

            newWindow.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSongList(ArrayList<SongModel> songModels){
        this.songModels = songModels;

        searchForDups();
    }
}

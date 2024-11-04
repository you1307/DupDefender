package com.thetechnoobs.dupdefender;

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
import java.util.*;

public class FindDupsLoadingScreenController {

    private ArrayList<SongModel> songModels = new ArrayList<>();
    private ArrayList<ArrayList<SongModel>> duplicatsList = new ArrayList<>();
    public Label loadingProgress;

    private double progress = 0;

    public void initialize() {
        // Initialization code if needed
    }

    private void searchForDups() {
        Thread thread = new Thread(() -> {
            // Map to group songs by name and artist
            Map<String, List<SongModel>> songMap = new HashMap<>();

            // Group songs
            for (SongModel songModel : songModels) {
                String key = songModel.name.trim().toLowerCase() + "|" + songModel.artist.trim().toLowerCase();
                songMap.computeIfAbsent(key, k -> new ArrayList<>()).add(songModel);
            }

            // Set to keep track of processed song paths
            Set<String> processedPaths = new HashSet<>();

            // Iterate over each group of songs
            for (List<SongModel> group : songMap.values()) {
                // Remove duplicates within the same folder
                Map<String, SongModel> uniqueSongs = new HashMap<>();
                for (SongModel song : group) {
                    uniqueSongs.put(song.chartFolderPath, song);
                }

                // If more than one unique song exists, it's a duplicate group
                if (uniqueSongs.size() > 1) {
                    // Check if any of the songs have been processed
                    boolean alreadyProcessed = false;
                    for (SongModel song : uniqueSongs.values()) {
                        if (processedPaths.contains(song.chartFolderPath)) {
                            alreadyProcessed = true;
                            break;
                        }
                    }

                    if (!alreadyProcessed) {
                        // Add the group to duplicates list
                        duplicatsList.add(new ArrayList<>(uniqueSongs.values()));
                        // Mark songs as processed
                        for (SongModel song : uniqueSongs.values()) {
                            processedPaths.add(song.chartFolderPath);
                        }
                    }
                }
            }

            // Update the UI on the JavaFX Application Thread
            Platform.runLater(() -> {
                loadingProgress.setText("" + duplicatsList.size());
                openDupWindow();
                closeThisWindow();
            });
        });

        thread.start();
    }

    public void closeThisWindow() {
        Stage stage = (Stage) loadingProgress.getScene().getWindow();  // Replace loadingProgress with any Node in the scene
        stage.close();  // Close the current window
    }

    private void openDupWindow() {
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

    public void setSongList(ArrayList<SongModel> songModels) {
        this.songModels = songModels;
        searchForDups();
    }
}

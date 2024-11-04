package com.thetechnoobs.dupdefender.controllers;

import com.thetechnoobs.dupdefender.ChartVisualizer;
import com.thetechnoobs.dupdefender.Interfaces;
import com.thetechnoobs.dupdefender.MidiChartVisualizer;
import com.thetechnoobs.dupdefender.Tools;
import com.thetechnoobs.dupdefender.models.SongModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public class MainPageController {
    public Button openExplorerButton, regenChartVisualizationBtnID, lookForDupsBtnID;
    public ImageView albumCoverImgID, chartImgID;
    public Label songNameTxtID, albumNameTxtID, artistNameTxtID, charterNameTxtID;
    public ScrollPane scrollPaneChartNotesID;
    public ListView<SongModel> foundChartsListView;
    public VBox foldersToSearchVBoxPaneID;

    private ObservableList<SongModel> songList = FXCollections.observableArrayList();
    private ArrayList<String> foldersToSearch = new ArrayList<>();

    @FXML
    public void initialize() {
        regenChartVisualizationBtnID.setVisible(false);
        foundChartsListView.setItems(songList);

        Rectangle clip = new Rectangle(albumCoverImgID.getFitWidth(), albumCoverImgID.getFitHeight());
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        albumCoverImgID.setClip(clip);

        foundChartsListView.setCellFactory(param -> new ListCell<SongModel>() {
            private Node songItemView;
            private SongItemViewController songItemViewController;

            @Override
            protected void updateItem(SongModel songData, boolean empty) {
                super.updateItem(songData, empty);

                if (empty || songData == null) {
                    setGraphic(null);
                } else {
                    if (songItemView == null) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("songItemView.fxml"));
                        try {
                            songItemView = loader.load();
                            songItemViewController = loader.getController();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    songItemViewController.setData(songData);
                    setGraphic(songItemView);

                    songItemViewController.setSelected(isSelected());
                }
            }
        });

        foundChartsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                regenChartVisualizationBtnID.setVisible(true);
                displayChartPreviewImg(newSelection, false);
                displayChartInfo(newSelection);
            }else{
                regenChartVisualizationBtnID.setVisible(false);
            }
        });



//            @Override
//            protected void updateItem(SongModel songData, boolean empty) {
//                super.updateItem(songData, empty);
//
//                // Remove old listener if it exists
//                if (selectedListener != null) {
//                    selectedProperty().removeListener(selectedListener);
//                    selectedListener = null;
//                }
//
//                if (empty || songData == null) {
//                    if (songItemViewController != null) {
//                        songItemViewController.clearData();
//                    }
//                    setGraphic(null);
//                } else {
//                    if (songItemView == null) {
//                        FXMLLoader loader = new FXMLLoader(getClass().getResource("songItemView.fxml"));
//                        try {
//                            songItemView = loader.load();
//                            songItemViewController = loader.getController();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    songItemViewController.setData(songData);
//                    setGraphic(songItemView);
//
//                    // Update visuals based on selection
//                    songItemViewController.setSelected(isSelected());
//
//                    // Add listener to update visuals when selection changes
//                    selectedListener = (obs, wasSelected, isNowSelected) -> {
//                        songItemViewController.setSelected(isNowSelected);
//                        displayChartPreviewImg(songData);
//                        displayChartInfo(songData);
//                    };
//                    selectedProperty().addListener(selectedListener);
//                }
//            }
//        });

        Image fileExploreBtnImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("file_dock_search_fill.png")));
        ImageView imageView = new ImageView(fileExploreBtnImg);
        openExplorerButton.setGraphic(imageView);

        openExplorerButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
                    try {
                        openFileExplorer();
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        regenChartVisualizationBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED){
                    SongModel songModel = songList.get(foundChartsListView.getSelectionModel().getSelectedIndex());
                    displayChartPreviewImg(songModel, true);
                }
            }
        });


        lookForDupsBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED){
                    openDuplicateSearchWindow();
                }
            }
        });

    }

    private void openDuplicateSearchWindow() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("find_dups_loading_screen.fxml"));
            Parent root = fxmlLoader.load();

            Stage newWindow = new Stage();
            newWindow.setTitle("Looking for dups");
            newWindow.setScene(new Scene(root));

            FindDupsLoadingScreenController findDupsLoadingScreenController = fxmlLoader.getController();
            findDupsLoadingScreenController.setSongList(new ArrayList<SongModel>(songList));

            newWindow.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayChartInfo(SongModel songData) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Image albumImg = new Image(songData.albumImgPath);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        albumCoverImgID.setImage(null);
                        albumCoverImgID.setImage(albumImg);

                        songNameTxtID.setText(songData.name);
                        artistNameTxtID.setText(songData.artist);
                        charterNameTxtID.setText(songData.charter);
                        albumNameTxtID.setText(songData.extraData.get("album"));
                    }
                });
            }
        });

        thread.start();
    }

    private void displayChartPreviewImg(SongModel songModel, Boolean forceGen) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String format = "png";
                File outputFile = new File(Path.of(songModel.chartFolderPath, "/chart_visualization."+format).toString());

                if (!outputFile.exists() || forceGen) {
                    System.out.println("generate img");
                    if (songModel.chartType == SongModel.CHART) {
                        try {
                            BufferedImage chartBitmap = ChartVisualizer.generateChartBitmap(Path.of(songModel.chartFolderPath, "/notes.chart").toString());

                            ImageIO.write(chartBitmap, format, outputFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (songModel.chartType == SongModel.MID) {
                        try {
                            BufferedImage bitmap = MidiChartVisualizer.generateChartBitmap(Path.of(songModel.chartFolderPath, "/notes.mid").toString());

                            ImageIO.write(bitmap, format, outputFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                Image image = new Image(outputFile.toURI().toString());
                scrollPaneChartNotesID.setMaxWidth(image.getWidth()+15);
                chartImgID.setFitHeight(image.getHeight());
                chartImgID.setFitWidth(image.getWidth());

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        chartImgID.setImage(null);
                        chartImgID.setImage(image);
                        scrollPaneChartNotesID.setVvalue(1.0);
                    }
                });

            }
        });

        thread.start();
    }

    public void updateFoldersToSearchList(){
        foldersToSearchVBoxPaneID.getChildren().clear();

        foldersToSearch.forEach(folderPath -> {
            foldersToSearchVBoxPaneID.getChildren().add(createFolderNode(folderPath));
        });
    }

    public Node createFolderNode(String folderPath){
        Node node = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("folder_to_search_view.fxml"));

        try {
            node = loader.load();
            FolderToSearchItemController folderToSearchItemController = loader.getController();
            folderToSearchItemController.setData(folderPath);
            folderToSearchItemController.setRemoveListener(new Interfaces.action() {
                @Override
                public void onRemovePathClicked(String path) {
                    foldersToSearch.remove(path);
                    updateFoldersToSearchList();
                }
            });
        }catch (IOException e) {
            e.printStackTrace();
        }

        return node;
    }

    private void openFileExplorer() throws IOException, URISyntaxException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a folder");
        File selectedDirectory = directoryChooser.showDialog(new Stage());


        if (selectedDirectory != null) {
            Thread searthThread = new Thread() {
                @Override
                public void run() {
                    ArrayList<SongModel> songModels = new ArrayList<>();

                    songModels.addAll(Tools.findAndExtractSongInfo(selectedDirectory.getAbsolutePath()));
                    foldersToSearch.add(selectedDirectory.getAbsolutePath());


                    System.out.println(songModels.size());

                    songModels.forEach(songData -> {
                        Platform.runLater(() -> {
                            updateFoldersToSearchList();
                            songList.add(songData);
                        });
                    });
                }
            };


            searthThread.start();
        }
    }
}
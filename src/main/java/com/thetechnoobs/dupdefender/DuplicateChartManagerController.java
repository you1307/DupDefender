package com.thetechnoobs.dupdefender.controllers;

import com.thetechnoobs.dupdefender.ChartVisualizer;
import com.thetechnoobs.dupdefender.MidiChartVisualizer;
import com.thetechnoobs.dupdefender.models.SongModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class DuplicateChartManagerController {

    //left song stuff
    public Label leftChartNameTxtID, leftChartArtistTxtID, leftChartCharterTxtID, leftChartAlbumTxtID;
    public ImageView leftChartAlbumImgID, leftChartImgID;
    public ScrollPane leftChartImgScrollViewID;

    // center pane stuff
    public ListView<SongModel> duplicateChartsListviewID;
    public ListView<ArrayList<SongModel>> duplicateChartsFoundListViewID;
    public ImageView sendChartRightImgBtnID, sendChartLeftImgBtnID;

    //right song stuff
    public Label rightChartNameTxtID, rightChartAlbumTxtID, rightChartCharterTxtID, rightChartArtistTxtID;
    public ImageView rightChartAlbumImgID, rightChartImgID;
    public ScrollPane rightChartImgScrollViewID;

    private ObservableList<ArrayList<SongModel>> dupListModels = FXCollections.observableArrayList();
    private ObservableList<SongModel> chartDupsList = FXCollections.observableArrayList();

    SongModel curLeftSongModel, curRightSongModel;

    public void initialize(){
        setupMainDuplicateChartsList();
        setupDuplicateSongModelsList();

        sendChartLeftImgBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!duplicateChartsListviewID.getSelectionModel().isEmpty()){
                    curLeftSongModel = duplicateChartsListviewID.getSelectionModel().getSelectedItem();
                    duplicateChartsListviewID.getItems().remove(curLeftSongModel);
                    setLeftSongModel();
                }
            }
        });

        sendChartRightImgBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                curRightSongModel = duplicateChartsListviewID.getSelectionModel().getSelectedItem();
                duplicateChartsListviewID.getItems().remove(curRightSongModel);
                setRightSongModel();
            }
        });
    }

    private void setRightSongModel() {
        clearChartInfo(2);

        rightChartAlbumImgID.setImage(new Image(curRightSongModel.albumImgPath));
        rightChartImgID.setImage(null);
        rightChartAlbumTxtID.setText(curRightSongModel.extraData.get("album"));
        rightChartArtistTxtID.setText(curRightSongModel.artist);
        rightChartNameTxtID.setText(curRightSongModel.name);
        rightChartCharterTxtID.setText(curRightSongModel.charter);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String format = "png";
                File outputFile = new File(Path.of(curRightSongModel.chartFolderPath, "/chart_visualization."+format).toString());

                if (!outputFile.exists()) {
                    System.out.println("generate img");
                    if (curRightSongModel.chartType == SongModel.CHART) {
                        try {
                            BufferedImage chartBitmap = ChartVisualizer.generateChartBitmap(Path.of(curRightSongModel.chartFolderPath, "/notes.chart").toString());

                            ImageIO.write(chartBitmap, format, outputFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (curRightSongModel.chartType == SongModel.MID) {
                        try {
                            BufferedImage bitmap = MidiChartVisualizer.generateChartBitmap(Path.of(curRightSongModel.chartFolderPath, "/notes.mid").toString());

                            ImageIO.write(bitmap, format, outputFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                Image image = new Image(outputFile.toURI().toString());
                rightChartImgScrollViewID.setMaxWidth(image.getWidth()+15);
                rightChartImgID.setFitHeight(image.getHeight());
                rightChartImgID.setFitWidth(image.getWidth());

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        rightChartImgID.setImage(image);
                        rightChartImgScrollViewID.setVvalue(1.0);
                    }
                });

            }
        });

        thread.start();
    }

    private void setLeftSongModel() {
        clearChartInfo(1);

        leftChartAlbumImgID.setImage(new Image(curLeftSongModel.albumImgPath));
        leftChartImgID.setImage(null);
        leftChartAlbumTxtID.setText(curLeftSongModel.extraData.get("album"));
        leftChartArtistTxtID.setText(curLeftSongModel.artist);
        leftChartNameTxtID.setText(curLeftSongModel.name);
        leftChartCharterTxtID.setText(curLeftSongModel.charter);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String format = "png";
                File outputFile = new File(Path.of(curLeftSongModel.chartFolderPath, "/chart_visualization."+format).toString());

                if (!outputFile.exists()) {
                    System.out.println("generate img");
                    if (curLeftSongModel.chartType == SongModel.CHART) {
                        try {
                            BufferedImage chartBitmap = ChartVisualizer.generateChartBitmap(Path.of(curLeftSongModel.chartFolderPath, "/notes.chart").toString());

                            ImageIO.write(chartBitmap, format, outputFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (curLeftSongModel.chartType == SongModel.MID) {
                        try {
                            BufferedImage bitmap = MidiChartVisualizer.generateChartBitmap(Path.of(curLeftSongModel.chartFolderPath, "/notes.mid").toString());

                            ImageIO.write(bitmap, format, outputFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                Image image = new Image(outputFile.toURI().toString());
                leftChartImgScrollViewID.setMaxWidth(image.getWidth()+15);
                leftChartImgID.setFitHeight(image.getHeight());
                leftChartImgID.setFitWidth(image.getWidth());

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        leftChartImgID.setImage(image);
                        leftChartImgScrollViewID.setVvalue(1.0);
                    }
                });

            }
        });

        thread.start();
    }

    public void clearChartInfo(int side){
        //1 = left : 2 = right

        if(side == 1){
            leftChartAlbumImgID.setImage(null);
            leftChartImgID.setImage(null);
            leftChartAlbumTxtID.setText("Album");
            leftChartArtistTxtID.setText("Artist");
            leftChartNameTxtID.setText("Name");
            leftChartCharterTxtID.setText("Charter");
        }else if(side == 2){
            rightChartAlbumImgID.setImage(null);
            rightChartImgID.setImage(null);
            rightChartAlbumTxtID.setText("Album");
            rightChartArtistTxtID.setText("Artist");
            rightChartNameTxtID.setText("Name");
            rightChartCharterTxtID.setText("Charter");
        }
    }

    private void setupDuplicateSongModelsList() {
        // settup list of individual dups found
        duplicateChartsListviewID.setItems(chartDupsList);

        duplicateChartsListviewID.setCellFactory(param -> new ListCell<>(){
            private Node songItemView;
            private SongItemViewController songItemViewController;

            @Override
            protected void updateItem(SongModel songModel, boolean empty) {
                super.updateItem(songModel, empty);

                if (empty || songModel == null) {
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
                    songItemViewController.setData(songModel);
                    songItemViewController.setRootPaneWidth(80);
                    setGraphic(songItemView);

                    songItemViewController.setSelected(isSelected());
                }
            }
        });
    }

    private void setupMainDuplicateChartsList() {
        //setup charts that have duplicates listview
        duplicateChartsFoundListViewID.setItems(dupListModels);

        duplicateChartsFoundListViewID.setCellFactory(param -> new ListCell<ArrayList<SongModel>>() {
            private Node songItemView;
            private DuplicateSongItemViewController duplicateSongItemViewController;

            @Override
            protected void updateItem(ArrayList<SongModel> songModelArrayList, boolean empty) {
                super.updateItem(songModelArrayList, empty);

                if (empty || songModelArrayList == null) {
                    setGraphic(null);
                } else {
                    if (songItemView == null) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("duplicateSongItemView.fxml"));
                        try {
                            songItemView = loader.load();
                            duplicateSongItemViewController = loader.getController();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    duplicateSongItemViewController.setData(songModelArrayList);
                    setGraphic(songItemView);

                    duplicateSongItemViewController.setSelected(isSelected());
                }
            }
        });

        duplicateChartsFoundListViewID.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(newSelection != null){
                showDupChartList(newSelection);
            }
        });
    }

    private void showDupChartList(ArrayList<SongModel> songModels) {
        chartDupsList.clear();
        chartDupsList.addAll(songModels);
    }


    public void setDupListModels(ObservableList<ArrayList<SongModel>> dupListModels){
        this.dupListModels.setAll(dupListModels);
    }

}

package com.thetechnoobs.dupdefender;

import com.thetechnoobs.dupdefender.models.SongModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class ChartVisulizerController {
    ImageView chartImgViewID;
    MenuButton instermentMenuID, difficultyMenuID;
    ScrollPane scrollPaneChartNotesID;
    SongModel curSongModel;
    String selectedInsterment, selectedDiff;

    public ChartVisulizerController(ImageView chartImgViewID, MenuButton intermentMenuID, MenuButton difficultyMenuID, ScrollPane scrollPaneChartNotesID){
        this.chartImgViewID = chartImgViewID;
        this.instermentMenuID = intermentMenuID;
        this.difficultyMenuID = difficultyMenuID;
        this.scrollPaneChartNotesID = scrollPaneChartNotesID;
    }


    public void setSongData(SongModel songModel){
        this.curSongModel = songModel;

        chartImgViewID.setImage(null);

        try {
            updateChartTracks(songModel);
            instermentMenuID.getItems().getFirst().fire();
            difficultyMenuID.getItems().getFirst().fire();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateChartTracks(SongModel songModel) throws Exception {
        Map<String, Set<String>> chartInstermentsAndDiff;

        if(songModel.chartType == SongModel.CHART){
            chartInstermentsAndDiff = ChartVisualizer.getAvailableInstrumentsAndDifficulties(Path.of(songModel.chartFolderPath, "notes.chart").toString());
        }else if(songModel.chartType == SongModel.MID){
            chartInstermentsAndDiff = MidiChartVisualizer.getAvailableInstrumentsAndDifficulties(Path.of(songModel.chartFolderPath, "notes.mid").toString());
        } else {
            chartInstermentsAndDiff = null;
        }

        difficultyMenuID.getItems().clear();
        instermentMenuID.getItems().clear();

        for(String insterment: chartInstermentsAndDiff.keySet()){
            MenuItem intermentItem = new MenuItem(insterment);
            instermentMenuID.getItems().add(intermentItem);

            intermentItem.setOnAction(actionEvent -> {
                difficultyMenuID.getItems().clear();
                selectedInsterment = insterment;
                instermentMenuID.setText(insterment);
                Set<String> difficulties = chartInstermentsAndDiff.get(insterment);

                for (String difficulty : difficulties) {
                    MenuItem difficultyItem = new MenuItem(difficulty);
                    difficultyMenuID.getItems().add(difficultyItem);

                    difficultyItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            selectedDiff = difficulty;
                            difficultyMenuID.setText(difficulty);
                            displayChartPreviewImg(songModel, true, selectedInsterment, selectedDiff);
                        }
                    });
                }

                difficultyMenuID.getItems().getFirst().fire();
            });
        }


    }

    private void displayChartPreviewImg(SongModel songModel, Boolean forceGen, String insterment, String difficulty) {
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
                            BufferedImage bitmap = MidiChartVisualizer.generateChartBitmap(Path.of(songModel.chartFolderPath, "/notes.mid").toString(), insterment, difficulty);
                            ImageIO.write(bitmap, format, outputFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                Image image = new Image(outputFile.toURI().toString());
                scrollPaneChartNotesID.setMaxWidth(image.getWidth()+15);
                chartImgViewID.setFitHeight(image.getHeight());
                chartImgViewID.setFitWidth(image.getWidth());

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        chartImgViewID.setImage(null);
                        chartImgViewID.setImage(image);
                        scrollPaneChartNotesID.setVvalue(1.0);
                    }
                });

            }
        });

        thread.start();
    }
}

package com.thetechnoobs.dupdefender;

import com.thetechnoobs.dupdefender.models.SongModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

public class DuplicateChartManagerController {
    public final static int RIGHT_SIDE = 1;
    public final static int LEFT_SIDE = 2;

    //left song stuff
    public Label leftChartNameTxtID, leftChartArtistTxtID, leftChartCharterTxtID, leftChartAlbumTxtID;
    public ImageView leftChartAlbumImgID, leftChartImgID, sendLeftChartCenterImgBtnID, sendRightChartCenterImgBtnID;
    public ImageView deleteLeftCharterImgBtnID;
    public ImageView leftGuitarDiffImgID, leftBassDiffImgID, leftDrumDiffImgID, leftLyricDiffImgID, leftKeysDiffImgID;
    public Label leftGuitarDiffLabelID, leftBassDiffLabelID, leftKeysDiffLabelID, leftDrumDiffLabelID, leftLyricDiffLabelID;
    public ScrollPane leftChartImgScrollViewID;
    public Pane extraChartInfoLeftPaneID;
    public VBox extraChartInfoLeftVboxID;
    public MenuButton leftInstermentMenuID, leftDifficultyMenuID;
    public HBox leftMediaHboxID;
    public Label leftChartPathFolderLabelID;
    private ChartVisulizerController leftChartVisulizerController;

    // center pane stuff
    public ListView<SongModel> duplicateChartsListviewID;
    public ListView<ArrayList<SongModel>> duplicateChartsFoundListViewID;
    public ImageView sendChartRightImgBtnID, sendChartLeftImgBtnID;

    //right song stuff
    public Label rightChartNameTxtID, rightChartAlbumTxtID, rightChartCharterTxtID, rightChartArtistTxtID;
    public Label rightGuitarDiffLabelID, rightBassDiffLabelID, rightDrumDiffLabelID, rightLyricDiffLabelID, rightKeysDiffLabelID;
    public ImageView rightChartAlbumImgID, rightChartImgID;
    public ImageView deleteRightCharterImgBtnID;
    public ImageView rightGuitarDiffImgID, rightBassDiffImgID, rightDrumDiffImgID, rightKeysDiffImgID, rightLyricDiffImgID;
    public ScrollPane rightChartImgScrollViewID;
    public VBox extraChartInfoRightVboxID;
    public MenuButton rightInstermentMenuID, rightDifficultyMenuID;
    public HBox rightMediaHboxID;
    private ChartVisulizerController rightChartVisulizerController;
    public Label rightChartPathFolderLabelID;


    //the charts that have dups
    private ObservableList<ArrayList<SongModel>> dupListModels = FXCollections.observableArrayList();

    //individual duplicate charts
    private ObservableList<SongModel> chartDupsList = FXCollections.observableArrayList();

    SongModel curLeftSongModel, curRightSongModel;

    public void initialize(){
        setupMainDuplicateChartsList();
        setupDuplicateSongModelsList();

        rightChartVisulizerController = new ChartVisulizerController(rightChartImgID, rightInstermentMenuID, rightDifficultyMenuID, rightChartImgScrollViewID);
        leftChartVisulizerController = new ChartVisulizerController(leftChartImgID, leftInstermentMenuID, leftDifficultyMenuID, leftChartImgScrollViewID);

        sendChartLeftImgBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!duplicateChartsListviewID.getSelectionModel().isEmpty()){
                    if(curLeftSongModel != null){
                        returnSongModelToCenter(LEFT_SIDE);
                    }

                    curLeftSongModel = duplicateChartsListviewID.getSelectionModel().getSelectedItem();
                    duplicateChartsListviewID.getItems().remove(curLeftSongModel);

                    Set<String> supportedInsterments = null;
                    if(curLeftSongModel.chartType == SongModel.CHART){
                        try {
                            supportedInsterments = InstrumentDetector.getSupportedInstruments(Path.of(curLeftSongModel.chartFolderPath, "notes.chart").toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            supportedInsterments = InstrumentDetector.getSupportedInstruments(Path.of(curLeftSongModel.chartFolderPath, "notes.mid").toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    setLeftSongModel(supportedInsterments);
                }
            }
        });

        sendChartRightImgBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(curRightSongModel != null){
                    returnSongModelToCenter(RIGHT_SIDE);
                }

                curRightSongModel = duplicateChartsListviewID.getSelectionModel().getSelectedItem();
                duplicateChartsListviewID.getItems().remove(curRightSongModel);

                Set<String> supportedInsterments = null;
                if(curRightSongModel.chartType == SongModel.CHART){
                    try {
                        supportedInsterments = InstrumentDetector.getSupportedInstruments(Path.of(curRightSongModel.chartFolderPath, "notes.chart").toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        supportedInsterments = InstrumentDetector.getSupportedInstruments(Path.of(curRightSongModel.chartFolderPath, "notes.mid").toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                setRightSongModel(supportedInsterments);
            }
        });

        sendLeftChartCenterImgBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                returnSongModelToCenter(LEFT_SIDE);
            }
        });

        sendRightChartCenterImgBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                returnSongModelToCenter(RIGHT_SIDE);
            }
        });

        deleteRightCharterImgBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(curRightSongModel != null){
                    dupListModels.get(duplicateChartsFoundListViewID.getSelectionModel().getSelectedIndex()).remove(curRightSongModel);
                    chartDupsList.remove(curRightSongModel);
                    Tools.deleteFolder(curRightSongModel.chartFolderPath);

                    clearChartInfo(RIGHT_SIDE);
                    duplicateChartsFoundListViewID.refresh();
                }
            }
        });

        deleteLeftCharterImgBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                dupListModels.get(duplicateChartsFoundListViewID.getSelectionModel().getSelectedIndex()).remove(curLeftSongModel);
                chartDupsList.remove(curLeftSongModel);
                Tools.deleteFolder(curLeftSongModel.chartFolderPath);

                clearChartInfo(LEFT_SIDE);
                duplicateChartsFoundListViewID.refresh();
            }
        });


    }

    private void returnSongModelToCenter(int side){
        switch (side){
            case LEFT_SIDE:
                chartDupsList.add(curLeftSongModel);
                curLeftSongModel = null;
                clearChartInfo(LEFT_SIDE);
                break;
            case RIGHT_SIDE:
                chartDupsList.add(curRightSongModel);
                curRightSongModel = null;
                clearChartInfo(RIGHT_SIDE);
                break;
        }
    }

    private void setRightSongModel(Set<String> supportedInsterments) {
        clearChartInfo(RIGHT_SIDE);

        rightChartAlbumImgID.setImage(new Image(curRightSongModel.albumImgPath));
        rightChartImgID.setImage(null);
        rightChartAlbumTxtID.setText(curRightSongModel.extraData.get("album"));
        rightChartArtistTxtID.setText(curRightSongModel.artist);
        rightChartNameTxtID.setText(curRightSongModel.name);
        rightChartCharterTxtID.setText(curRightSongModel.charter);
        rightChartPathFolderLabelID.setText(curRightSongModel.chartFolderPath);

        curRightSongModel.extraData.forEach((key, val) -> {
            Label label = new Label(key + ": " + val);

            extraChartInfoRightVboxID.getChildren().add(label);
        });

        rightChartVisulizerController.setSongData(curRightSongModel);
        rightMediaHboxID.getChildren().addAll(MediaHandler.getMedia(curRightSongModel.chartFolderPath));

        if(supportedInsterments.contains("guitar")){
            rightGuitarDiffImgID.setImage(new Image(getClass().getResourceAsStream("guitarDiffIcon.png")));
            rightGuitarDiffLabelID.setText(curRightSongModel.extraData.get("diff_guitar"));
        }else{
            rightGuitarDiffImgID.setImage(new Image(getClass().getResourceAsStream("guitarDiffIconNone.png")));
            rightGuitarDiffLabelID.setText(null);
        }

        if(supportedInsterments.contains("bass")){
            rightBassDiffImgID.setImage(new Image(getClass().getResourceAsStream("bassDiffIcon.png")));
            rightBassDiffLabelID.setText(curRightSongModel.extraData.get("diff_guitar"));
        }else{
            rightBassDiffImgID.setImage(new Image(getClass().getResourceAsStream("bassDiffIconNone.png")));
            rightBassDiffLabelID.setText(null);
        }

        if(supportedInsterments.contains("drums")){
            rightDrumDiffImgID.setImage(new Image(getClass().getResourceAsStream("drumDiffIcon.png")));
            rightDrumDiffLabelID.setText(curRightSongModel.extraData.get("diff_drums"));
        }else{
            rightDrumDiffImgID.setImage(new Image(getClass().getResourceAsStream("drumDiffIconNone.png")));
            rightDrumDiffLabelID.setText(null);
        }

        if(supportedInsterments.contains("vocals")){
            rightLyricDiffImgID.setImage(new Image(getClass().getResourceAsStream("lyricsDiff.png")));
            rightLyricDiffLabelID.setText(curRightSongModel.extraData.get("diff_vocals"));
        }else{
            rightLyricDiffImgID.setImage(new Image(getClass().getResourceAsStream("lyricsDiffNone.png")));
            rightLyricDiffLabelID.setText(null);
        }

        if(supportedInsterments.contains("keys")){
            rightKeysDiffImgID.setImage(new Image(getClass().getResourceAsStream("keysDiffIcon.png")));
            rightKeysDiffLabelID.setText(curRightSongModel.extraData.get("diff_keys"));
        }else{
            rightKeysDiffImgID.setImage(new Image(getClass().getResourceAsStream("keysDiffIconNone.png")));
            rightKeysDiffLabelID.setText(curRightSongModel.extraData.get("diff_keys"));
        }
    }

    private void setLeftSongModel(Set<String> supportedInsterments) {
        clearChartInfo(LEFT_SIDE);

        leftChartAlbumImgID.setImage(new Image(curLeftSongModel.albumImgPath));
        leftChartImgID.setImage(null);
        leftChartAlbumTxtID.setText(curLeftSongModel.extraData.get("album"));
        leftChartArtistTxtID.setText(curLeftSongModel.artist);
        leftChartNameTxtID.setText(curLeftSongModel.name);
        leftChartCharterTxtID.setText(curLeftSongModel.charter);
        leftChartPathFolderLabelID.setText(curLeftSongModel.chartFolderPath);

        curLeftSongModel.extraData.forEach((key, val) -> {
            Label label = new Label(key + ": " + val);

            extraChartInfoLeftVboxID.getChildren().add(label);
        });

        leftChartVisulizerController.setSongData(curLeftSongModel);
        leftMediaHboxID.getChildren().addAll(MediaHandler.getMedia(curLeftSongModel.chartFolderPath));

        if(supportedInsterments.contains("guitar")){
            leftGuitarDiffImgID.setImage(new Image(getClass().getResourceAsStream("guitarDiffIcon.png")));
            leftGuitarDiffLabelID.setText(curLeftSongModel.extraData.get("diff_guitar"));
        }else{
            leftGuitarDiffImgID.setImage(new Image(getClass().getResourceAsStream("guitarDiffIconNone.png")));
            leftGuitarDiffLabelID.setText(null);
        }

        if(supportedInsterments.contains("bass")){
            leftBassDiffImgID.setImage(new Image(getClass().getResourceAsStream("bassDiffIcon.png")));
            leftBassDiffLabelID.setText(curLeftSongModel.extraData.get("diff_guitar"));
        }else{
            leftBassDiffImgID.setImage(new Image(getClass().getResourceAsStream("bassDiffIconNone.png")));
            leftBassDiffLabelID.setText(null);
        }

        if(supportedInsterments.contains("drums")){
            leftDrumDiffImgID.setImage(new Image(getClass().getResourceAsStream("drumDiffIcon.png")));
            leftDrumDiffLabelID.setText(curLeftSongModel.extraData.get("diff_drums"));
        }else{
            leftDrumDiffImgID.setImage(new Image(getClass().getResourceAsStream("drumDiffIconNone.png")));
            leftDrumDiffLabelID.setText(null);
        }

        if(supportedInsterments.contains("vocals")){
            leftLyricDiffImgID.setImage(new Image(getClass().getResourceAsStream("lyricsDiff.png")));
            leftLyricDiffLabelID.setText(curLeftSongModel.extraData.get("diff_vocals"));
        }else{
            leftLyricDiffImgID.setImage(new Image(getClass().getResourceAsStream("lyricsDiffNone.png")));
            leftLyricDiffLabelID.setText(null);
        }

        if(supportedInsterments.contains("keys")){
            leftKeysDiffImgID.setImage(new Image(getClass().getResourceAsStream("keysDiffIcon.png")));
            leftKeysDiffLabelID.setText(curLeftSongModel.extraData.get("diff_keys"));
        }else{
            leftKeysDiffImgID.setImage(new Image(getClass().getResourceAsStream("keysDiffIconNone.png")));
            leftKeysDiffLabelID.setText(curLeftSongModel.extraData.get("diff_keys"));
        }
    }

    public void clearChartInfo(int side){
        switch (side){
            case LEFT_SIDE:
                extraChartInfoLeftVboxID.getChildren().clear();
                leftChartAlbumImgID.setImage(null);
                leftChartImgID.setImage(null);
                leftChartAlbumTxtID.setText("Album");
                leftChartArtistTxtID.setText("Artist");
                leftChartNameTxtID.setText("Name");
                leftChartCharterTxtID.setText("Charter");
                leftGuitarDiffImgID.setImage(new Image(getClass().getResourceAsStream("guitarDiffIcon.png")));
                leftBassDiffImgID.setImage(new Image(getClass().getResourceAsStream("bassDiffIcon.png")));
                leftDrumDiffImgID.setImage(new Image(getClass().getResourceAsStream("drumDiffIcon.png")));
                leftKeysDiffImgID.setImage(new Image(getClass().getResourceAsStream("keysDiffIcon.png")));
                leftLyricDiffImgID.setImage(new Image(getClass().getResourceAsStream("lyricsDiff.png")));
                leftKeysDiffLabelID.setText(null);
                leftGuitarDiffLabelID.setText(null);
                leftBassDiffLabelID.setText(null);
                leftDrumDiffLabelID.setText(null);
                leftLyricDiffLabelID.setText(null);
                leftMediaHboxID.getChildren().clear();
                leftChartPathFolderLabelID.setText("path/to/folder");
                break;
            case RIGHT_SIDE:
                extraChartInfoRightVboxID.getChildren().clear();
                rightChartAlbumImgID.setImage(null);
                rightChartImgID.setImage(null);
                rightChartAlbumTxtID.setText("Album");
                rightChartArtistTxtID.setText("Artist");
                rightChartNameTxtID.setText("Name");
                rightChartCharterTxtID.setText("Charter");
                rightGuitarDiffImgID.setImage(new Image(getClass().getResourceAsStream("guitarDiffIcon.png")));
                rightBassDiffImgID.setImage(new Image(getClass().getResourceAsStream("bassDiffIcon.png")));
                rightDrumDiffImgID.setImage(new Image(getClass().getResourceAsStream("drumDiffIcon.png")));
                rightKeysDiffImgID.setImage(new Image(getClass().getResourceAsStream("keysDiffIcon.png")));
                rightLyricDiffImgID.setImage(new Image(getClass().getResourceAsStream("lyricsDiff.png")));
                rightKeysDiffLabelID.setText(null);
                rightGuitarDiffLabelID.setText(null);
                rightBassDiffLabelID.setText(null);
                rightDrumDiffLabelID.setText(null);
                rightLyricDiffLabelID.setText(null);
                rightMediaHboxID.getChildren().clear();
                rightChartPathFolderLabelID.setText("path/to/folder");
                break;
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
                clearChartInfo(RIGHT_SIDE);
                clearChartInfo(LEFT_SIDE);
                curLeftSongModel = null;
                curRightSongModel = null;

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

package com.thetechnoobs.dupdefender;

import com.thetechnoobs.dupdefender.models.SongModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MainPageController {
    public Button openExplorerButton, regenChartVisualizationBtnID, lookForDupsBtnID;
    public ImageView albumCoverImgID, chartImgID;
    public Label songNameTxtID, albumNameTxtID, artistNameTxtID, charterNameTxtID;
    public ScrollPane scrollPaneChartNotesID;
    public ListView<SongModel> foundChartsListView;
    public VBox foldersToSearchVBoxPaneID;
    public MenuButton difficultyMenuID, instermentMenuID;
    public Label guitarDiffLabelID, bassDiffLabelID, lyricDiffLabelID, drumDiffLabelID, keysDiffLabelID;
    public ImageView guitarDiffImgID, bassDiffImgID, drumDiffImgID, lyricDiffImgID, keysDiffImgID;
    public HBox mediaHboxID;
    public ProgressIndicator progressIndicator;
    public Label progressTxtWarningLabelID;
    public VBox progressVboxWarnID;

    private ObservableList<SongModel> songList = FXCollections.observableArrayList();
    private ArrayList<String> foldersToSearch = new ArrayList<>();
    private ChartVisulizerController chartVisulizerController;

    @FXML
    public void initialize() {
        regenChartVisualizationBtnID.setVisible(false);
        foundChartsListView.setItems(songList);

        Rectangle clip = new Rectangle(albumCoverImgID.getFitWidth(), albumCoverImgID.getFitHeight());
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        albumCoverImgID.setClip(clip);

        chartVisulizerController = new ChartVisulizerController(chartImgID, instermentMenuID, difficultyMenuID, scrollPaneChartNotesID);

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
                chartVisulizerController.setSongData(newSelection);
                displayChartInfo(newSelection);
            }else{
                regenChartVisualizationBtnID.setVisible(false);
            }
        });

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
                    chartVisulizerController.setSongData(songModel);
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

                        mediaHboxID.getChildren().clear();
                        List<Node> nodes = MediaHandler.getMedia(songData.chartFolderPath);
                        mediaHboxID.getChildren().addAll(nodes);

                        Set<String> supportedInsterments = null;
                        try{
                            if(songData.chartType == SongModel.CHART){
                                supportedInsterments = InstrumentDetector.getSupportedInstruments(Path.of(songData.chartFolderPath, "notes.chart").toString());
                            }else{
                                supportedInsterments = InstrumentDetector.getSupportedInstruments(Path.of(songData.chartFolderPath, "notes.mid").toString());
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        if(supportedInsterments == null){
                            return;
                        }


                        if(supportedInsterments.contains("guitar")){
                            guitarDiffImgID.setImage(new Image(getClass().getResourceAsStream("guitarDiffIcon.png")));
                            guitarDiffLabelID.setText(songData.extraData.get("diff_guitar"));
                        }else{
                            guitarDiffImgID.setImage(new Image(getClass().getResourceAsStream("guitarDiffIconNone.png")));
                            guitarDiffLabelID.setText(null);
                        }

                        if(supportedInsterments.contains("bass")){
                            bassDiffImgID.setImage(new Image(getClass().getResourceAsStream("bassDiffIcon.png")));
                            bassDiffLabelID.setText(songData.extraData.get("diff_guitar"));
                        }else{
                            bassDiffImgID.setImage(new Image(getClass().getResourceAsStream("bassDiffIconNone.png")));
                            bassDiffLabelID.setText(null);
                        }

                        if(supportedInsterments.contains("drums")){
                            drumDiffImgID.setImage(new Image(getClass().getResourceAsStream("drumDiffIcon.png")));
                            drumDiffLabelID.setText(songData.extraData.get("diff_drums"));
                        }else{
                            drumDiffImgID.setImage(new Image(getClass().getResourceAsStream("drumDiffIconNone.png")));
                            drumDiffLabelID.setText(null);
                        }

                        if(supportedInsterments.contains("vocals")){
                            lyricDiffImgID.setImage(new Image(getClass().getResourceAsStream("lyricsDiff.png")));
                            lyricDiffLabelID.setText(songData.extraData.get("diff_vocals"));
                        }else{
                            lyricDiffImgID.setImage(new Image(getClass().getResourceAsStream("lyricsDiffNone.png")));
                            lyricDiffLabelID.setText(null);
                        }

                        if(supportedInsterments.contains("keys")){
                            keysDiffImgID.setImage(new Image(getClass().getResourceAsStream("keysDiffIcon.png")));
                            keysDiffLabelID.setText(songData.extraData.get("diff_keys"));
                        }else{
                            keysDiffImgID.setImage(new Image(getClass().getResourceAsStream("keysDiffIconNone.png")));
                            keysDiffLabelID.setText(songData.extraData.get("diff_keys"));
                        }
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
            Task<Void> searchTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    ArrayList<SongModel> songModels = new ArrayList<>();

                    songModels.addAll(Tools.findAndExtractSongInfo(selectedDirectory.getAbsolutePath()));
                    foldersToSearch.add(selectedDirectory.getAbsolutePath());

                    System.out.println(songModels.size());

                    Platform.runLater(() -> {
                        songList.clear();
                        updateFoldersToSearchList();
                    });

                    int totalSongs = songModels.size();
                    int processedSongs = 0;

                    for (SongModel songData : songModels) {
                        if (isCancelled()) {
                            break;
                        }
                        Platform.runLater(() -> {
                            songList.add(songData);
                        });
                        processedSongs++;
                        updateProgress(processedSongs, totalSongs);
                    }

                    return null;
                }
            };

            progressIndicator.visibleProperty().bind(searchTask.runningProperty());
            progressTxtWarningLabelID.visibleProperty().bind(searchTask.runningProperty());
            progressVboxWarnID.visibleProperty().bind(searchTask.runningProperty());

            progressIndicator.progressProperty().bind(searchTask.progressProperty());

            searchTask.setOnFailed(event -> {
                Throwable exception = searchTask.getException();
                exception.printStackTrace();
            });

            Thread searchThread = new Thread(searchTask);
            searchThread.setDaemon(true);
            searchThread.start();
        }
    }
}
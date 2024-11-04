package com.thetechnoobs.dupdefender;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MediaHandler {

    // Supported image and video file extensions
    private static final String[] IMAGE_EXTENSIONS = { ".jpg", ".jpeg", ".png", ".gif", ".bmp" };
    private static final String[] VIDEO_EXTENSIONS = { ".mp4", ".mkv", ".avi", ".mov", ".flv", ".wmv" };

    public static List<Node> getMedia(String pathToChartFolder) {
        List<Node> mediaNodes = new ArrayList<>();

        File folder = new File(pathToChartFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Invalid folder path: " + pathToChartFolder);
            return mediaNodes;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            System.err.println("Could not list files in folder: " + pathToChartFolder);
            return mediaNodes;
        }

        for (File file : files) {
            String fileName = file.getName().toLowerCase();
            if(fileName.contains("visualization")){
                continue;
            }

            if (file.isFile()) {
                if (isImageFile(fileName)) {
                    // Create ImageView for image
                    ImageView imageView = createImageView(file);
                    if (imageView != null) {
                        addOpenFileOnClick(imageView, file);
                        mediaNodes.add(imageView);
                    }
                } else if (isVideoFile(fileName)) {
                    // Create ImageView with video thumbnail and play button overlay
                    Node videoNode = createVideoThumbnail(file);
                    if (videoNode != null) {
                        addOpenFileOnClick(videoNode, file);
                        mediaNodes.add(videoNode);
                    }
                }
            }
        }

        return mediaNodes;
    }

    private static boolean isImageFile(String fileName) {
        return Arrays.stream(IMAGE_EXTENSIONS).anyMatch(fileName::endsWith);
    }

    private static boolean isVideoFile(String fileName) {
        return Arrays.stream(VIDEO_EXTENSIONS).anyMatch(fileName::endsWith);
    }

    private static ImageView createImageView(File imageFile) {
        try {
            ImageView imageView = new ImageView(imageFile.toURI().toString());
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(100); // Set desired width
            imageView.setFitHeight(100); // Set desired height
            return imageView;
        } catch (Exception e) {
            System.err.println("Error loading image: " + imageFile.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }

    private static Node createVideoThumbnail(File videoFile) {
        StackPane stackPane = new StackPane();

        // Create MediaView
        Media media = new Media(videoFile.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);

        // Set MediaView properties
        mediaView.setFitWidth(100);
        mediaView.setFitHeight(100);
        mediaView.setPreserveRatio(true);

        // Add MediaView to StackPane
        stackPane.getChildren().add(mediaView);

        // Create play button icon
        Polygon playButton = new Polygon();
        playButton.getPoints().addAll(
                0.0, 0.0,
                0.0, 30.0,
                25.0, 15.0
        );
        playButton.setFill(Color.WHITE);
        playButton.setOpacity(0.7);
        StackPane.setAlignment(playButton, Pos.CENTER);
        stackPane.getChildren().add(playButton);

        // Generate thumbnail using JavaFX MediaPlayer
        mediaPlayer.setOnReady(() -> {
            // Seek to 1 second into the video
            mediaPlayer.seek(Duration.seconds(1));
        });

        mediaPlayer.setOnError(() -> {
            System.err.println("Error loading media: " + media.getSource());
            mediaPlayer.dispose();

            // Optionally, display a placeholder image
            Platform.runLater(() -> {
                ImageView placeholder = new ImageView("/path/to/placeholder.png");
                placeholder.setPreserveRatio(true);
                placeholder.setFitWidth(100);
                placeholder.setFitHeight(100);
                stackPane.getChildren().remove(mediaView);
                stackPane.getChildren().add(0, placeholder);
            });
        });

        mediaPlayer.setOnPlaying(() -> {
            int durationToPlay = 0;
            if(media.getDuration().toMillis() > 5000){
                durationToPlay = 5000;
            }else{
                durationToPlay = 300;
            }

            // Wait a short time to ensure the frame is rendered
            PauseTransition pause = new PauseTransition(Duration.millis(durationToPlay));
            pause.setOnFinished(event -> {
                // Capture snapshot
                WritableImage snapshot = mediaView.snapshot(new SnapshotParameters(), null);
                if (snapshot != null) {
                    // Create ImageView with the snapshot
                    ImageView thumbnailView = new ImageView(snapshot);
                    thumbnailView.setPreserveRatio(true);
                    thumbnailView.setFitWidth(100);
                    thumbnailView.setFitHeight(100);

                    // Replace MediaView with thumbnailView
                    stackPane.getChildren().remove(mediaView);
                    stackPane.getChildren().add(0, thumbnailView);

                    // Dispose of the media player
                    mediaPlayer.dispose();
                } else {
                    System.err.println("Failed to capture snapshot for: " + videoFile.getAbsolutePath());
                    mediaPlayer.dispose();
                }
            });
            pause.play();
        });

        mediaPlayer.play();
        mediaPlayer.setMute(true); // Mute the audio

        return stackPane;
    }



    private static Node createVideoThumbnailOld(File videoFile) {
        StackPane stackPane = new StackPane();

        // Create MediaView
        Media media = new Media(videoFile.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);

        // Set MediaView properties
        mediaView.setFitWidth(100);
        mediaView.setFitHeight(100);
        mediaView.setPreserveRatio(true);
        mediaView.setOpacity(0.01); // Make it almost invisible

        // Add MediaView to StackPane
        stackPane.getChildren().add(mediaView);

        // Create play button icon
        Polygon playButton = new Polygon();
        playButton.getPoints().addAll(
                0.0, 0.0,
                0.0, 30.0,
                25.0, 15.0
        );
        playButton.setFill(Color.WHITE);
        playButton.setOpacity(0.7);
        StackPane.setAlignment(playButton, Pos.CENTER);
        stackPane.getChildren().add(playButton);

        // Generate thumbnail using JavaFX MediaPlayer
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.seek(Duration.seconds(1));
        });

        mediaPlayer.setOnError(() -> {
            System.err.println("Error loading media: " + media.getSource());
            mediaPlayer.dispose();
        });


        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (newTime.toSeconds() >= 1.0) {
                mediaPlayer.pause();

                // Take snapshot of MediaView
                Platform.runLater(() -> {
                    WritableImage snapshot = mediaView.snapshot(new SnapshotParameters(), null);
                    if (snapshot != null) {
                        ImageView thumbnailView = new ImageView(snapshot);
                        thumbnailView.setPreserveRatio(true);
                        thumbnailView.setFitWidth(100);
                        thumbnailView.setFitHeight(100);

                        // Replace MediaView with thumbnailView
                        stackPane.getChildren().remove(mediaView);
                        stackPane.getChildren().add(0, thumbnailView);

                        // Dispose of the media player
                        mediaPlayer.dispose();
                    } else {
                        System.err.println("Failed to capture snapshot for: " + videoFile.getAbsolutePath());
                        mediaPlayer.dispose();
                    }
                });
            }
        });

        mediaPlayer.play();
        mediaPlayer.setMute(true); // Mute the audio

        return stackPane;
    }

    private static void addOpenFileOnClick(Node node, File file) {
        node.setOnMouseClicked(event -> {
            openFileWithDefaultApplication(file);
        });
    }

    private static void openFileWithDefaultApplication(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                System.err.println("Desktop is not supported. Cannot open file: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error opening file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
}

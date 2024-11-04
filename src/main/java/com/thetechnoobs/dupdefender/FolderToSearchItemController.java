package com.thetechnoobs.dupdefender;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class FolderToSearchItemController {
    public Label folderPathTxtID;
    public ImageView removeFolderBtnID;
    private String path;

    private Interfaces.action onRemoveClicked;

    public void initialize(){
        removeFolderBtnID.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(onRemoveClicked != null){
                    onRemoveClicked.onRemovePathClicked(path);
                }
            }
        });
    }

    public void setRemoveListener(Interfaces.action onClick){
        onRemoveClicked = onClick;
    }

    public void setData(String folderPath){
        this.path = folderPath;

        folderPathTxtID.setText(folderPath);
    }
}

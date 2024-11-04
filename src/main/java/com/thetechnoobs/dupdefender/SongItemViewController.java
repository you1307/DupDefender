package com.thetechnoobs.dupdefender.controllers;

import com.thetechnoobs.dupdefender.models.SongModel;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class SongItemViewController {
    public Label songNameTxtID, charterTxtID, artistTxtID;
    public ImageView albumImgID;
    public AnchorPane rootPaneID;

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    private SongModel songData;

    private void updateData() {
        if (songData.hasAlbum()) {
            try {
                albumImgID.setImage(new Image(songData.albumImgPath));
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            albumImgID.setImage(null);
        }

        songNameTxtID.setText(songData.name);
        charterTxtID.setText(songData.charter);
        artistTxtID.setText(songData.artist);
    }

    public void setSelected(boolean selected) {
        rootPaneID.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
    }

    public void setRootPaneWidth(double width){
        rootPaneID.setPrefWidth(width);
    }


    public void setData(SongModel songData) {
        this.songData = songData;
        updateData();
    }

    public void clearData() {
        albumImgID.setImage(null);
        rootPaneID.setStyle(null);
        songNameTxtID.setText(null);
        charterTxtID.setText(null);
        artistTxtID.setText(null);
    }
}


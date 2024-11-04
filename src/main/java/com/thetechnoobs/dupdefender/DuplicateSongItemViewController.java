package com.thetechnoobs.dupdefender;

import com.thetechnoobs.dupdefender.models.SongModel;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

public class DuplicateSongItemViewController {
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    public Label songNameTxtID, artistTxtID, dupCountTxtID;
    public ImageView albumImgID;
    public AnchorPane rootPaneID;

    ArrayList<SongModel> duplicateSongModels;

    private void updateData() {
        if (duplicateSongModels.getFirst().hasAlbum()) {
            try {
                albumImgID.setImage(new Image(duplicateSongModels.getFirst().albumImgPath));
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            albumImgID.setImage(null);
        }

        songNameTxtID.setText(duplicateSongModels.getFirst().name);
        artistTxtID.setText(duplicateSongModels.getFirst().artist);
        dupCountTxtID.setText("X" + duplicateSongModels.size());
    }

    public void setData(ArrayList<SongModel> songModelArrayList) {
        this.duplicateSongModels = songModelArrayList;
        updateData();
    }

    public void clearData() {
        albumImgID.setImage(null);
        rootPaneID.setStyle(null);
        songNameTxtID.setText(null);
        artistTxtID.setText(null);
    }

    public void setSelected(boolean selected) {
        rootPaneID.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
    }
}

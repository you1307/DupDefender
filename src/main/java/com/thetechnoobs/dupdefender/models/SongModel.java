package com.thetechnoobs.dupdefender.models;

import java.util.Map;

public class SongModel {

    public static int MID = 1;
    public static int CHART = 2;

    public String name;
    public String charter;
    public String artist;
    public String albumImgPath;
    public String chartFolderPath;
    public Map<String, String> extraData;
    public int chartType;

    public boolean hasAlbum() {
        return albumImgPath != null;
    }
}
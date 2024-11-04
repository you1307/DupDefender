package com.thetechnoobs.fishtank.dupdestroyer;

import org.ini4j.Ini;
import org.ini4j.Profile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class Tools {
    public static JSONArray findAndExtractSongInfo(String dirToSearch) {
        JSONArray jsonArray = new JSONArray();

        try (Stream<Path> paths = Files.walk(Paths.get(dirToSearch))) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".ini")).forEach(path -> {
                try {
                    JSONObject songJsonObj = createSongJsonObj(path.toString());
                    jsonArray.put(songJsonObj);
                } catch (Exception e) {
                    //System.err.println(e.getMessage() + " Path: "+ path.toString());
                    //TODO handle broken ini file case
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return jsonArray;
    }

    private static JSONObject createSongJsonObj(String pathToini) throws IOException {
        Ini ini = new Ini();
        ini.getConfig().setMultiOption(true);
        ini.load(new File(pathToini));


        Profile.Section dataSection = Optional.ofNullable(ini.get("Song")).orElseGet(() -> ini.get("song"));

        JSONObject songJsonObj = new JSONObject();

        songJsonObj.put("name", Jsoup.parse(dataSection.get("name")).text());
        songJsonObj.put("artist", Jsoup.parse(dataSection.get("artist")).text());
        songJsonObj.put("charter", Jsoup.parse(dataSection.get("charter")).text());
        songJsonObj.put("album", addAlbumPath(pathToini).toString());

        return songJsonObj;
    }

    private static Path addAlbumPath(String pathToIni){
        try (Stream<Path> paths = Files.walk(Path.of(pathToIni).getParent())){
            return paths.filter(path -> path.getFileName().toString().equals("album.jpg")).findFirst().orElse(null);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

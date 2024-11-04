package com.thetechnoobs.dupdefender;

import com.thetechnoobs.dupdefender.models.SongModel;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Tools {
    public static ArrayList<SongModel> findAndExtractSongInfo(String dirToSearch) {
        ArrayList<SongModel> songModels = new ArrayList<>();
        final int[] errFiles = {0};

        try (Stream<Path> paths = Files.walk(Paths.get(dirToSearch))) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".ini")).forEach(path -> {
                try {
                    SongModel songModel = createSongModelFromIni(path.toString());
                    songModels.add(songModel);
                } catch (Exception e) {
                    e.printStackTrace();
                    errFiles[0]++;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("num of error files: " + errFiles[0]);
        return songModels;
    }

    public static SongModel createSongModelFromIni(String pathToIni) throws IOException {
        // Detect the encoding of the INI file
        Charset iniFileEncoding = EncodingDetector.detectFileEncoding(pathToIni);

        // Parse the INI file using the detected encoding
        Map<String, Map<String, String>> iniData = IniParserUtil.parseIniFileToMap(pathToIni, iniFileEncoding);

        // Retrieve the 'song' section (case-insensitive)
        Map<String, String> dataSection = iniData.getOrDefault("song", iniData.get("Song"));

        if (dataSection == null) {
            throw new IOException("No [song] section found in INI file: " + pathToIni);
        }

        SongModel songModel = new SongModel();

        songModel.albumImgPath = addAlbumPath(pathToIni).toUri().toString();
        songModel.name = Jsoup.parse(dataSection.getOrDefault("name", "")).text();
        songModel.artist = Jsoup.parse(dataSection.getOrDefault("artist", "")).text();
        songModel.charter = Jsoup.parse(dataSection.getOrDefault("charter", "")).text();
        songModel.chartFolderPath = Path.of(pathToIni).getParent().toString();
        songModel.chartType = findChartType(songModel.chartFolderPath);

        // Store extra data
        songModel.extraData = dataSection;

        return songModel;
    }

    private static int findChartType(String pathToFolder) {
        try (Stream<Path> paths = Files.walk(Path.of(pathToFolder))) {
            if (paths.anyMatch(path -> path.getFileName().toString().equalsIgnoreCase("notes.chart"))) {
                return SongModel.CHART;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Stream<Path> paths = Files.walk(Path.of(pathToFolder))) {
            if (paths.anyMatch(path -> path.getFileName().toString().equalsIgnoreCase("notes.mid"))) {
                return SongModel.MID;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1; // Return a default value if neither file is found
    }


    private static SongModel createSongModelFromIniOld(String pathToIni) throws IOException{
        Ini ini = new Ini();
        ini.getConfig().setMultiOption(true);
        ini.load(new File(pathToIni));

        Profile.Section dataSection = Optional.ofNullable(ini.get("Song")).orElseGet(() -> ini.get("song"));

        SongModel songModel = new SongModel();

        songModel.albumImgPath = addAlbumPath(pathToIni).toString();
        songModel.name = Jsoup.parse(dataSection.get("name")).text();
        songModel.artist = Jsoup.parse(dataSection.get("artist")).text();
        songModel.charter = Jsoup.parse(dataSection.get("charter")).text();

        return songModel;
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
            return paths.filter(path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                return fileName.equals("album.jpg") || fileName.equals("album.png");
            }).findFirst().orElse(null);

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

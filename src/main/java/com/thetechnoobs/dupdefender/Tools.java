package com.thetechnoobs.dupdefender;

import com.thetechnoobs.dupdefender.models.SongModel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
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

    public static void deleteFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            System.out.println("Folder does not exist: " + folderPath);
            return;
        }

        deleteFolderContents(folder);

        if (folder.delete()) {
            System.out.println("Deleted folder: " + folderPath);
        } else {
            System.out.println("Failed to delete folder: " + folderPath);
        }
    }

    private static void deleteFolderContents(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteFolderContents(child);
            }
        }
        if (file.delete()) {
            System.out.println("Deleted: " + file.getPath());
        } else {
            System.out.println("Failed to delete: " + file.getPath());
        }
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
        songModel.name = dataSection.getOrDefault("name", "");
        songModel.artist = dataSection.getOrDefault("artist", "");
        songModel.charter = dataSection.getOrDefault("charter", "");
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

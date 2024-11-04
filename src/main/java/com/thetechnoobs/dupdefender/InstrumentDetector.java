package com.thetechnoobs.dupdefender;

import javax.sound.midi.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class InstrumentDetector {

    // Function to get supported instruments from a song file (.chart or .mid)
    public static Set<String> getSupportedInstruments(String songFilePath) throws Exception {
        Set<String> supportedInstruments = new HashSet<>();

        Path path = Paths.get(songFilePath);
        String extension = getFileExtension(path.getFileName().toString()).toLowerCase();

        if (extension.equals(".chart")) {
            supportedInstruments = getInstrumentsFromChartFile(songFilePath);
        } else if (extension.equals(".mid") || extension.equals(".midi")) {
            supportedInstruments = getInstrumentsFromMidiFile(songFilePath);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + extension);
        }

        return supportedInstruments;
    }

    // Helper function to get file extension
    private static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index == -1 ? "" : fileName.substring(index);
    }

    // Function to get instruments from a .chart file
    private static Set<String> getInstrumentsFromChartFile(String chartFilePath) throws IOException {
        Set<String> instruments = new HashSet<>();

        Map<String, String> instrumentNameMap = getInstrumentNameMap();

        List<String> lines = Files.readAllLines(Paths.get(chartFilePath));

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("[") && line.endsWith("]")) {
                String sectionName = line.substring(1, line.length() - 1).toUpperCase();

                // Remove difficulty prefixes
                sectionName = sectionName.replaceAll("^(EASY|MEDIUM|HARD|EXPERT)", "");

                // Remove any trailing "EVENTS" or similar
                sectionName = sectionName.replaceAll("EVENTS$", "").trim();

                // Map to standard instrument name
                for (String key : instrumentNameMap.keySet()) {
                    if (sectionName.contains(key)) {
                        instruments.add(instrumentNameMap.get(key));
                        break;
                    }
                }
            }
        }

        return instruments;
    }

    // Function to get instruments from a .mid file
    private static Set<String> getInstrumentsFromMidiFile(String midiFilePath) throws Exception {
        Set<String> instruments = new HashSet<>();

        Map<String, String> instrumentNameMap = getInstrumentNameMap();

        // Open the MIDI file
        Sequence sequence = MidiSystem.getSequence(new File(midiFilePath));

        // Iterate over tracks
        for (Track track : sequence.getTracks()) {
            String trackName = getTrackName(track);
            if (trackName == null || trackName.trim().isEmpty()) {
                // Unnamed track, skip or handle as needed
                continue;
            }

            trackName = trackName.toUpperCase();

            // Map to standard instrument name
            for (String key : instrumentNameMap.keySet()) {
                if (trackName.contains(key)) {
                    instruments.add(instrumentNameMap.get(key));
                    break;
                }
            }
        }

        return instruments;
    }

    // Helper function to get track name from MIDI track
    private static String getTrackName(Track track) {
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage message = event.getMessage();
            if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage) message;
                if (metaMessage.getType() == 0x03) { // Track Name
                    return new String(metaMessage.getData());
                }
            }
        }
        return null;
    }

    // Helper function to get instrument name mapping
    private static Map<String, String> getInstrumentNameMap() {
        Map<String, String> instrumentNameMap = new HashMap<>();
        instrumentNameMap.put("GUITAR", "guitar");
        instrumentNameMap.put("SINGLE", "guitar");
        instrumentNameMap.put("LEAD", "guitar");
        instrumentNameMap.put("T1 GEMS", "guitar"); // For "T1 GEMS"
        instrumentNameMap.put("BASS", "bass");
        instrumentNameMap.put("DOUBLEBASS", "bass");
        instrumentNameMap.put("RHYTHM", "bass");
        instrumentNameMap.put("DRUMS", "drums");
        instrumentNameMap.put("KEYS", "keys");
        instrumentNameMap.put("VOCALS", "vocals");
        instrumentNameMap.put("HARMONY", "vocals");
        return instrumentNameMap;
    }
}


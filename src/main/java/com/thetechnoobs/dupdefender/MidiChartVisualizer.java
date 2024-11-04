package com.thetechnoobs.dupdefender;

import com.thetechnoobs.dupdefender.models.ChartData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.sound.midi.*;

public class MidiChartVisualizer {
    // Define minimum sustain duration threshold
    private static final int MIN_SUSTAIN_TICKS = 5;

    public static BufferedImage generateChartBitmap(String midiFilePath, String instrument, String difficulty) throws Exception {
        ChartData chartData = parseMidiFile(midiFilePath, instrument, difficulty);

        // Generate the bitmap image
        BufferedImage bitmap = ChartVisualizer.createBitmap(chartData);

        return bitmap;
    }

    private static ChartData parseMidiFile(String midiFilePath, String instrument, String difficulty) throws Exception {
        ChartData chartData = new ChartData();

        Sequence sequence = MidiSystem.getSequence(new File(midiFilePath));

        // Get resolution (ticks per quarter note)
        chartData.resolution = sequence.getResolution();

        // Construct the track name to search for
        String trackNameToFind = getTrackNameForInstrument(instrument);

        // Identify the desired track(s)
        boolean desiredTrackFound = false;
        for (Track track : sequence.getTracks()) {
            // Get track name
            String trackName = getTrackName(track);
            if (trackName != null && trackName.equalsIgnoreCase(trackNameToFind)) {
                desiredTrackFound = true;
                parseTrack(track, chartData, difficulty);
                break; // Stop after finding the desired track
            }
        }

        if (!desiredTrackFound) {
            System.out.println("Desired track not found: " + trackNameToFind);
            throw new Exception("Desired track not found: " + trackNameToFind);
        }

        return chartData;
    }

    public static Map<String, Set<String>> getAvailableInstrumentsAndDifficulties(String midiFilePath) throws Exception {
        Map<String, Set<String>> instrumentDifficulties = new HashMap<>();

        Sequence sequence = MidiSystem.getSequence(new File(midiFilePath));

        for (Track track : sequence.getTracks()) {
            String trackName = getTrackName(track);
            if (trackName == null || trackName.trim().isEmpty()) {
                trackName = "Unnamed Track";
            }

            if (isInstrumentTrack(trackName)) {
                String instrument = extractInstrumentFromTrackName(trackName);

                Set<String> difficulties = instrumentDifficulties.computeIfAbsent(instrument, k -> new HashSet<>());

                Set<String> trackDifficulties = getDifficultiesFromTrack(track);
                difficulties.addAll(trackDifficulties);
            }
        }

        return instrumentDifficulties;
    }

    private static String getTrackNameForInstrument(String instrument) {
        return "PART " + instrument.toUpperCase();
    }

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

    //check if a track is an instrument track
    private static boolean isInstrumentTrack(String trackName) {
        // Define known instrument track names
        List<String> instrumentTrackPrefixes = Arrays.asList("PART", "T1 GEMS");
        for (String prefix : instrumentTrackPrefixes) {
            if (trackName.toUpperCase().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static String extractInstrumentFromTrackName(String trackName) {
        // Remove "PART" prefix and trim
        String instrument = trackName.replaceFirst("(?i)PART", "").trim();
        return instrument.isEmpty() ? "Unknown Instrument" : instrument;
    }

    private static Set<String> getDifficultiesFromTrack(Track track) {
        Set<String> difficulties = new HashSet<>();
        Set<Integer> noteNumbers = new HashSet<>();

        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage message = event.getMessage();

            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int command = sm.getCommand();

                if (command == ShortMessage.NOTE_ON || command == ShortMessage.NOTE_OFF) {
                    int noteNumber = sm.getData1();
                    noteNumbers.add(noteNumber);
                }
            }
        }

        // Determine difficulties based on note ranges
        if (containsNotesInRange(noteNumbers, 96, 100)) {
            difficulties.add("EXPERT");
        }
        if (containsNotesInRange(noteNumbers, 84, 88)) {
            difficulties.add("HARD");
        }
        if (containsNotesInRange(noteNumbers, 72, 76)) {
            difficulties.add("MEDIUM");
        }
        if (containsNotesInRange(noteNumbers, 60, 64)) {
            difficulties.add("EASY");
        }

        return difficulties;
    }

    private static boolean containsNotesInRange(Set<Integer> noteNumbers, int start, int end) {
        for (int note : noteNumbers) {
            if (note >= start && note <= end) {
                return true;
            }
        }
        return false;
    }

    private static void parseTrack(Track track, ChartData chartData, String difficulty) {
        Map<Integer, MidiEvent> noteOnMap = new HashMap<>();

        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage message = event.getMessage();
            long tick = event.getTick();

            if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage) message;
                if (metaMessage.getType() == 0x51) { // Set Tempo
                    byte[] data = metaMessage.getData();
                    int tempo = ((data[0] & 0xFF) << 16) |
                            ((data[1] & 0xFF) << 8) |
                            (data[2] & 0xFF);
                    // Tempo is in microseconds per quarter note
                    chartData.tempoEvents.add(new ChartVisualizer.TempoEvent(tick, tempo));
                }
            } else if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int command = sm.getCommand();
                int noteNumber = sm.getData1();

                if (command == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                    // Note on
                    noteOnMap.put(noteNumber, event);
                } else if (command == ShortMessage.NOTE_OFF || (command == ShortMessage.NOTE_ON && sm.getData2() == 0)) {
                    // Note off
                    MidiEvent onEvent = noteOnMap.remove(noteNumber);
                    if (onEvent != null) {
                        long startTick = onEvent.getTick();
                        int duration = (int) (tick - startTick);

                        int fret = mapMidiNoteToFret(noteNumber, difficulty);
                        if (fret >= 0) {
                            // Apply minimum sustain duration threshold
                            if (duration < MIN_SUSTAIN_TICKS) {
                                duration = 0;
                            }

                            //System.out.println("Note parsed: Tick=" + startTick + ", Fret=" + fret + ", Duration=" + duration);
                            chartData.noteEvents.add(new ChartVisualizer.NoteEvent(startTick, fret, duration));
                        }
                    }
                }
            }
        }
    }

    private static int mapMidiNoteToFret(int noteNumber, String difficulty) {
        int baseNoteNumber;
        switch (difficulty.toUpperCase()) {
            case "EASY":
                baseNoteNumber = 60; // C4
                break;
            case "MEDIUM":
                baseNoteNumber = 72; // C5
                break;
            case "HARD":
                baseNoteNumber = 84; // C6
                break;
            case "EXPERT":
                baseNoteNumber = 96; // C7
                break;
            default:
                // Invalid difficulty
                return -1;
        }

        if (noteNumber == baseNoteNumber) {
            return 0; // Green
        } else if (noteNumber == baseNoteNumber + 1) {
            return 1; // Red
        } else if (noteNumber == baseNoteNumber + 2) {
            return 2; // Yellow
        } else if (noteNumber == baseNoteNumber + 3) {
            return 3; // Blue
        } else if (noteNumber == baseNoteNumber + 4) {
            return 4; // Orange
        } else if (noteNumber == baseNoteNumber - 1) {
            return 7; // Open note
        } else {
            return -1; // Ignore other notes
        }
    }
}

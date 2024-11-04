package com.thetechnoobs.dupdefender;

import com.thetechnoobs.dupdefender.models.ChartData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.List;

public class ChartVisualizer {

    // Method to parse the .chart file and generate the bitmap
    public static BufferedImage generateChartBitmap(String chartFilePath) throws IOException {
        // Parse the .chart file
        ChartData chartData = parseChartFile(chartFilePath);

        // Generate the bitmap image
        BufferedImage bitmap = createBitmap(chartData);

        return bitmap;
    }

    // Class to represent tempo changes
    public static class TempoEvent {
        long tick;
        long bpm; // Microseconds per quarter note

        public TempoEvent(long tick, long bpm) {
            this.tick = tick;
            this.bpm = bpm;
        }
    }

    // Class to represent notes
    public static class NoteEvent {
        long tick;
        int fret; // 0-4 for normal notes
        int duration;

        public NoteEvent(long tick, int fret, int duration) {
            this.tick = tick;
            this.fret = fret;
            this.duration = duration;
        }
    }


    // Method to parse the .chart file
    private static ChartData parseChartFile(String chartFilePath) throws IOException {
        ChartData chartData = new ChartData();

        List<String> lines = Files.readAllLines(Paths.get(chartFilePath));

        Iterator<String> iterator = lines.iterator();
        String line;
        String currentSection = null;

        while (iterator.hasNext()) {
            line = iterator.next().trim();

            if (line.startsWith("[")) {
                currentSection = line;
            } else if (line.startsWith("Resolution")) {
                chartData.resolution = Integer.parseInt(line.split("=")[1].trim());
            } else if (currentSection != null) {
                if (currentSection.equals("[SyncTrack]")) {
                    if (line.endsWith("{")) {
                        // Start of SyncTrack section
                        while (iterator.hasNext()) {
                            line = iterator.next().trim();
                            if (line.equals("}")) {
                                break;
                            }
                            parseSyncTrackLine(line, chartData);
                        }
                    }
                } else if (currentSection.equals("[ExpertSingle]") || currentSection.equals("[ExpertDoubleBass]")) {
                    if (line.endsWith("{")) {
                        // Start of ExpertSingle section
                        while (iterator.hasNext()) {
                            line = iterator.next().trim();
                            if (line.equals("}")) {
                                break;
                            }
                            parseNoteLine(line, chartData);
                        }
                    }
                }
            }
        }

        return chartData;
    }

    // Method to parse SyncTrack lines
    private static void parseSyncTrackLine(String line, ChartData chartData) {
        if (line.contains("=")) {
            String[] parts = line.split("=");
            long tick = Long.parseLong(parts[0].trim());
            String[] eventParts = parts[1].trim().split(" ");
            String eventType = eventParts[0];
            if (eventType.equals("B")) {
                long bpm = Long.parseLong(eventParts[1]);
                chartData.tempoEvents.add(new TempoEvent(tick, bpm));
            }
            // Ignoring TS (Time Signature) events for simplicity
        }
    }

    // Method to parse Note lines
    private static void parseNoteLine(String line, ChartData chartData) {
        if (line.contains("=")) {
            String[] parts = line.split("=");
            long tick = Long.parseLong(parts[0].trim());
            String[] noteParts = parts[1].trim().split(" ");

            String eventType = noteParts[0];

            if (eventType.equals("N")) { // Note event
                int fret = Integer.parseInt(noteParts[1]);
                int duration = Integer.parseInt(noteParts[2]);
                chartData.noteEvents.add(new NoteEvent(tick, fret, duration));
            }
            // Handle other event types if necessary
        }
    }


    // Method to create the bitmap image
    public static BufferedImage createBitmap(ChartData chartData) {
        // Constants for visualization
        final int sustainTrailWidth = 3;
        final int laneCount = 5; // Standard frets 0-4
        final int noteWidth = 10; // Width of the note squares
        final int noteHeight = 5; // Height of the note squares
        final int pixelsPerSecond = 400; // Adjust this value as needed

        // Determine if there are open notes (fret 7)
        boolean hasOpenNotes = chartData.noteEvents.stream().anyMatch(note -> note.fret == 7);
        int totalLanes = hasOpenNotes ? laneCount + 1 : laneCount;

        // Calculate image width
        int laneWidth = noteWidth + 10; // Space between lanes
        int width = totalLanes * laneWidth + 20; // Total width with padding

        // Get time and tempo maps
        TimeAndTempo timeAndTempo = convertTicksToTime(chartData);
        Map<Long, Double> tickToTime = timeAndTempo.tickToTime;

        // Calculate total song duration in seconds
        double totalDuration = tickToTime.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0) / 1000.0;

        // Calculate the required image height
        int height = (int) (totalDuration * pixelsPerSecond) + 100; // Add padding

        // Create the image
        BufferedImage bitmap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bitmap.createGraphics();

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background color
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);

        // Define colors for frets
        Map<Integer, Color> fretColors = new HashMap<>();
        fretColors.put(0, Color.GREEN);
        fretColors.put(1, Color.RED);
        fretColors.put(2, Color.YELLOW);
        fretColors.put(3, Color.BLUE);
        fretColors.put(4, Color.decode("#ff8300"));
        if (hasOpenNotes) {
            fretColors.put(7, Color.MAGENTA); // open strum
        }

        // Map frets to lane indices
        Map<Integer, Integer> fretToLaneMap = new HashMap<>();
        fretToLaneMap.put(0, 0);
        fretToLaneMap.put(1, 1);
        fretToLaneMap.put(2, 2);
        fretToLaneMap.put(3, 3);
        fretToLaneMap.put(4, 4);
        if (hasOpenNotes) {
            fretToLaneMap.put(7, totalLanes - 1);
        }

        // Draw the fretboard lanes
        g2d.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= totalLanes; i++) {
            int x = 10 + i * laneWidth;
            g2d.drawLine(x, 0, x, height);
        }

        // Set stroke for sustains
        g2d.setStroke(new BasicStroke(sustainTrailWidth));

        // Draw the notes
        for (NoteEvent note : chartData.noteEvents) {
            // Get the start time of the note in seconds
            double startTimeMs = tickToTime.getOrDefault(note.tick, 0.0);
            double startTimeSec = startTimeMs / 1000.0;

            // Get the end time of the note in seconds
            long endTick = note.tick + note.duration;
            double endTimeMs = tickToTime.getOrDefault(endTick, startTimeMs);
            double endTimeSec = endTimeMs / 1000.0;

            int laneIndex = fretToLaneMap.getOrDefault(note.fret, -1);
            if (laneIndex == -1) {
                // Skip notes with unrecognized frets
                continue;
            }

            int x = 10 + laneIndex * laneWidth + (laneWidth - noteWidth) / 2;
            int y = height - (int) (startTimeSec * pixelsPerSecond) - noteHeight - 50; // Position from bottom, add padding

            // Set color based on fret
            Color noteColor = fretColors.getOrDefault(note.fret, Color.WHITE);
            g2d.setColor(noteColor);

            // Draw note head (square)
            g2d.fillRect(x, y, noteWidth, noteHeight);

            if (note.duration > 0) {
                // Calculate sustain height
                int sustainHeight = (int) ((endTimeSec - startTimeSec) * pixelsPerSecond);

                // Calculate endY (sustain's end point)
                int endY = y + noteHeight - sustainHeight;

                // Ensure the sustain doesn't go above the top of the image
                if (endY < 50) {
                    endY = 50; // Padding at the top
                }

                // Draw the stem (sustain) upwards from the bottom of the note head
                g2d.drawLine(
                        x + noteWidth / 2, // Center of the note head
                        y + noteHeight/2,    // Bottom of the note head
                        x + noteWidth / 2, // Same X position
                        endY               // End position of the sustain
                );
            }
        }

        // Reset stroke to default
        g2d.setStroke(new BasicStroke());

        // Draw time markers (updated)
        g2d.setColor(Color.GRAY);
        int timeMarkerInterval = 10; // Every 10 seconds
        for (int t = 0; t <= totalDuration; t += timeMarkerInterval) {
            int y = height - (int) (t * pixelsPerSecond) - 50; // Corrected Y-coordinate calculation
            g2d.drawLine(10, y, width - 10, y);
            g2d.drawString(String.format("%d sec", t), 5, y - 5);
        }

        g2d.dispose();
        return bitmap;
    }

    public static Map<String, Set<String>> getAvailableInstrumentsAndDifficulties(String chartFilePath) throws IOException {
        Map<String, Set<String>> instrumentDifficulties = new HashMap<>();

        List<String> lines = Files.readAllLines(Paths.get(chartFilePath), EncodingDetector.detectFileEncoding(chartFilePath));

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("[") && line.endsWith("]")) {
                String sectionName = line.substring(1, line.length() - 1);

                // Pattern: [DifficultyInstrument]
                // For example: [ExpertSingle], [HardDoubleBass]

                String difficulty = null;
                String instrument = null;

                if (sectionName.matches("^(Easy|Medium|Hard|Expert).*$")) {
                    // Difficulty is at the beginning
                    difficulty = sectionName.replaceAll("^(Easy|Medium|Hard|Expert).*", "$1").toUpperCase();
                    instrument = sectionName.substring(difficulty.length()).trim();
                } else {
                    // Handle other patterns or default cases
                    difficulty = "UNKNOWN";
                    instrument = sectionName;
                }

                if (instrument.isEmpty()) {
                    instrument = "Unknown Instrument";
                }

                // Initialize the set of difficulties for this instrument
                Set<String> difficulties = instrumentDifficulties.computeIfAbsent(instrument, k -> new HashSet<>());
                difficulties.add(difficulty);
            }
        }

        return instrumentDifficulties;
    }


    // Method to convert ticks to time in milliseconds
    private static class TimeAndTempo {
        Map<Long, Double> tickToTime;
        NavigableMap<Long, Double> tickToBPM;

        public TimeAndTempo(Map<Long, Double> tickToTime, NavigableMap<Long, Double> tickToBPM) {
            this.tickToTime = tickToTime;
            this.tickToBPM = tickToBPM;
        }
    }

    private static TimeAndTempo convertTicksToTime(ChartData chartData) {
        Map<Long, Double> tickToTime = new HashMap<>();
        NavigableMap<Long, Double> tickToBPM = new TreeMap<>();

        chartData.tempoEvents.sort(Comparator.comparingLong(e -> e.tick));

        double currentTime = 0.0;
        long lastTick = 0;
        double microsecondsPerBeat = 500000.0; // Default value for 120 BPM
        double bpm = 120.0; // Default BPM

        // Initialize the tickToBPM map with the default BPM
        tickToBPM.put(0L, bpm);

        Iterator<TempoEvent> tempoIterator = chartData.tempoEvents.iterator();
        TempoEvent nextTempoEvent = tempoIterator.hasNext() ? tempoIterator.next() : null;

        // Collect all relevant ticks (note starts, note ends, tempo changes)
        Set<Long> allTicks = new TreeSet<>();
        for (NoteEvent note : chartData.noteEvents) {
            allTicks.add(note.tick);
            allTicks.add(note.tick + note.duration);
        }
        for (TempoEvent tempoEvent : chartData.tempoEvents) {
            allTicks.add(tempoEvent.tick);
        }

        for (long tick : allTicks) {
            // Apply tempo changes at the correct tick
            while (nextTempoEvent != null && nextTempoEvent.tick == tick) {
                microsecondsPerBeat = nextTempoEvent.bpm;
                bpm = 60000000.0 / microsecondsPerBeat;
                tickToBPM.put(nextTempoEvent.tick, bpm);

                if (tempoIterator.hasNext()) {
                    nextTempoEvent = tempoIterator.next();
                } else {
                    nextTempoEvent = null;
                }
            }

            // Calculate time since the last tick
            long deltaTicks = tick - lastTick;
            double deltaTime = (deltaTicks * microsecondsPerBeat) / (chartData.resolution * 1000.0);
            currentTime += deltaTime;
            tickToTime.put(tick, currentTime);

            lastTick = tick;
        }

        return new TimeAndTempo(tickToTime, tickToBPM);
    }

}



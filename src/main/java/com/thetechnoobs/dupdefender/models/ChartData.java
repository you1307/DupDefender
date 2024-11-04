package com.thetechnoobs.dupdefender.models;

import com.thetechnoobs.dupdefender.ChartVisualizer;

import java.util.ArrayList;
import java.util.List;

public class ChartData {
        public List<ChartVisualizer.TempoEvent> tempoEvents = new ArrayList<>();
        public List<ChartVisualizer.NoteEvent> noteEvents = new ArrayList<>();
        public int resolution = 192; // Default resolution
}

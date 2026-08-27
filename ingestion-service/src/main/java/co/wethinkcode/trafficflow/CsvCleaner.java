package co.wethinkcode.trafficflow;

import com.opencsv.CSVReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvCleaner {

    private static final Set<String> MISSING_TOKENS = Set.of(
            "", "n/a", "tbd", "unknown", "-", "nan"
    );
    private static final Set<String> TRUE_TOKENS = Set.of("y", "yes", "true", "1");
    private static final Set<String> FALSE_TOKENS = Set.of("n", "no", "false", "0");

    /** Reads and cleans the CSV at the given classpath resource path. */
    public static List<Intersection> loadAndClean(String resourcePath) throws Exception {
        List<String[]> rows;
        try (InputStream in = CsvCleaner.class.getResourceAsStream(resourcePath);
             CSVReader reader = new CSVReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            rows = reader.readAll();
        }

        // First row is the header — skip it.
        Map<String, Intersection> byId = new LinkedHashMap<>();
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length < 4) continue; // skip malformed rows

            String rawId = row[0];
            String rawDistrict = row[1];
            String rawSignalType = row[2];
            String rawActive = row[3];

            String id = normalizeMissing(rawId);
            if (id == null) continue; // no usable ID, skip the row entirely

            String district = normalizeMissing(rawDistrict);
            String signalType = normalizeMissing(rawSignalType);
            if (signalType != null) {
                signalType = signalType.toLowerCase();
            }
            Boolean active = parseBoolean(rawActive);

            Intersection cleaned = new Intersection(id.toUpperCase(), district, signalType, active);

            // Dedup key: case-insensitive ID, so INT-1005 and int-1005 collapse.
            // Last write wins.
            byId.put(cleaned.getId(), cleaned);
        }

        return new ArrayList<>(byId.values());
    }

    /**
     * Trims and collapses internal whitespace; returns null for blanks or
     * known "no data" placeholders (N/A, TBD, unknown, -, NaN — any casing).
     */
    static String normalizeMissing(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim().replaceAll("\\s+", " ");
        if (MISSING_TOKENS.contains(trimmed.toLowerCase())) {
            return null;
        }
        return trimmed;
    }

    /**
     * Parses active_flag variants (Y/N, yes/no, 1/0, true/FALSE).
     * Returns null (not a guess) for unrecognized tokens like "unknown".
     */
    static Boolean parseBoolean(String raw) {
        if (raw == null) return null;
        String normalized = raw.trim().toLowerCase();
        if (TRUE_TOKENS.contains(normalized)) return true;
        if (FALSE_TOKENS.contains(normalized)) return false;
        return null;
    }
}
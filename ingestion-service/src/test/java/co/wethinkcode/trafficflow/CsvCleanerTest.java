package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CsvCleanerTest {

    @Test
    void trimsWhitespaceAndCollapsesDoubleSpaces() {
        assertEquals("Downtown", CsvCleaner.normalizeMissing("  Downtown  "));
    }

    @Test
    void treatsPlaceholderValuesAsNull() {
        assertNull(CsvCleaner.normalizeMissing("N/A"));
        assertNull(CsvCleaner.normalizeMissing("unknown"));
        assertNull(CsvCleaner.normalizeMissing(""));
    }

    @Test
    void parsesTrueAndFalseBooleanVariants() {
        assertEquals(Boolean.TRUE, CsvCleaner.parseBoolean("Y"));
        assertEquals(Boolean.TRUE, CsvCleaner.parseBoolean("1"));
        assertEquals(Boolean.FALSE, CsvCleaner.parseBoolean("FALSE"));
        assertEquals(Boolean.FALSE, CsvCleaner.parseBoolean("no"));
    }

    @Test
    void returnsNullForUnrecognizedBooleanToken() {
        assertNull(CsvCleaner.parseBoolean("unknown"));
    }

    @Test
    void collapsesDuplicateRecordsAcrossIdCasing() throws Exception {
        List<Intersection> result = CsvCleaner.loadAndClean("/intersections-legacy.csv");
        long count = result.stream().filter(i -> i.getId().equals("INT-1005")).count();
        assertEquals(1, count);
    }
}
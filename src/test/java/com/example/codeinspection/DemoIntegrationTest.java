package com.example.codeinspection;

import com.example.codeinspection.demo.Main;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DemoIntegrationTest {
    @Test
    void consoleRunsAllFiveReportsAndDisplaysCrossCheckedFintechResults() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            Main.main(new String[0]);
        } finally {
            System.setOut(original);
        }
        String console = output.toString(StandardCharsets.UTF_8);
        int position = -1;
        for (String heading : new String[]{"SECURITY AUDIT REPORT", "PROJECT STATISTICS REPORT",
                "QUALITY GATE REPORT", "COMPLIANCE REPORT", "TECHNICAL DEBT REPORT"}) {
            int next = console.indexOf(heading);
            assertTrue(next > position, "Missing or out-of-order report: " + heading);
            assertEquals(next, console.lastIndexOf(heading), "Duplicate report: " + heading);
            position = next;
        }
        assertTrue(console.contains("Issues: 5 | Critical: 1 | Security score: 0/100"));
        assertTrue(console.contains("Total files: 5"));
        assertTrue(console.contains("Total LOC (source + tests): 390"));
        assertTrue(console.contains("FINAL RESULT: FAILED"));
        assertTrue(console.contains("Violations: 4"));
        assertTrue(console.contains("OVERALL: NON-COMPLIANT"));
        assertTrue(console.contains("Estimated debt: 360 min (6.00 hours)"));
    }
}

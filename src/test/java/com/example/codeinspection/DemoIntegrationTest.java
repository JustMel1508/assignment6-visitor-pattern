package com.example.codeinspection;

import com.example.codeinspection.demo.Main;
import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.Dependency;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import com.example.codeinspection.visitor.ComplianceVisitor;
import com.example.codeinspection.visitor.ProjectStatisticsVisitor;
import com.example.codeinspection.visitor.QualityGateVisitor;
import com.example.codeinspection.visitor.SecurityAuditVisitor;
import com.example.codeinspection.visitor.TechnicalDebtVisitor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DemoIntegrationTest {
    @Test
    void allFiveVisitorsAgreeOnADeeplyNestedCleanProjectWithoutDoubleCounting() {
        Directory root = new Directory("root").add(new Directory("empty"));
        Directory current = root;
        for (int i = 0; i < 500; i++) {
            Directory next = new Directory("level" + i);
            current.add(next);
            current = next;
        }
        current.add(new SourceCodeFile("A", 10, 3, false, false, true, 0))
                .add(new SourceCodeFile("B", 20, 7, false, false, true, 0))
                .add(new TestFile("ATest", 3, "A"))
                .add(new TestFile("BTest", 7, "B"))
                .add(new ConfigurationFile("production", true, false, false, false))
                .add(new DependencyFile("pom.xml", List.of(new Dependency("sdk", "1", false))))
                .add(new DependencyFile("module/pom.xml", List.of(new Dependency("json", "2", false))));

        var statistics = new ProjectStatisticsVisitor().analyze(root);
        var security = new SecurityAuditVisitor().analyze(root);
        var quality = new QualityGateVisitor().analyze(root);
        var compliance = new ComplianceVisitor().analyze(root);
        var debt = new TechnicalDebtVisitor().analyze(root);
        assertEquals(7, statistics.fileCount());
        assertEquals(502, statistics.directoryCount());
        assertEquals(40, statistics.linesOfCode());
        assertEquals(2, statistics.dependencyCount());
        assertEquals(2, statistics.sourceFileCount());
        assertEquals(2, statistics.testFileCount());
        assertEquals(5.0, statistics.averageComplexity());
        assertEquals(1.0, statistics.testSourceRatio());
        assertEquals(0, security.issueCount());
        assertEquals(100, security.score());
        assertEquals(statistics, quality.statistics());
        assertEquals(security, quality.security());
        assertTrue(quality.passed());
        assertTrue(compliance.compliant());
        assertEquals(0, compliance.violationCount());
        assertTrue(debt.findings().isEmpty());
        assertEquals(2, debt.perFile().size());
        assertEquals(0, debt.estimatedMinutes());
    }

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

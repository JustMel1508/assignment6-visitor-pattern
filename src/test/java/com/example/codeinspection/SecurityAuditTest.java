package com.example.codeinspection;

import com.example.codeinspection.demo.FintechProject;
import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.Dependency;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.report.SecurityReport;
import com.example.codeinspection.report.Severity;
import com.example.codeinspection.visitor.SecurityAuditVisitor;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SecurityAuditTest {
    @Test
    void credentialsAndHighRiskGenerateIndependentFindingsWithCorrectSeverities() {
        SecurityReport report = new SecurityAuditVisitor().analyze(
                new SourceCodeFile("Payment.java", 10, 2, true, true, true, 0));
        assertEquals(2, report.issueCount());
        assertEquals(1, report.criticalIssueCount());
        assertEquals(List.of(Severity.CRITICAL, Severity.HIGH),
                report.findings().stream().map(f -> f.severity()).toList());
        assertEquals(40, report.score());
        assertTrue(report.findings().stream().allMatch(f -> f.element().equals("Payment.java")));
    }

    @Test
    void dependencyVulnerabilityComesFromMetadataAndIdentifiesEntry() {
        SecurityReport report = new SecurityAuditVisitor().analyze(new DependencyFile("pom.xml", List.of(
                new Dependency("org.example:safe", "1", false),
                new Dependency("org.example:old", null, true))));
        assertEquals(1, report.issueCount());
        assertEquals("pom.xml -> org.example:old", report.findings().get(0).element());
        assertEquals("VULNERABLE_DEPENDENCY", report.findings().get(0).rule());
        assertEquals(0, report.criticalIssueCount());
        assertEquals(80, report.score());
    }

    @Test
    void allConfigurationProblemsAreReportedAndDevelopmentDebugIsAllowed() {
        SecurityReport report = new SecurityAuditVisitor().analyze(
                new ConfigurationFile("production", true, true, true, true));
        assertEquals(3, report.issueCount());
        assertEquals(1, report.criticalIssueCount());
        assertEquals(30, report.score());
        assertEquals(0, new SecurityAuditVisitor().analyze(
                new ConfigurationFile("dev", false, true, false, false)).issueCount());
    }

    @Test
    void secureEmptyAndReusedAnalysesHaveNoStaleFindings() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        SecurityReport old = visitor.analyze(FintechProject.create());
        SecurityReport clean = visitor.analyze(new Directory("empty"));
        assertEquals(5, old.issueCount());
        assertEquals(0, clean.issueCount());
        assertEquals(100, clean.score());
        assertEquals(0, visitor.analyze(new SourceCodeFile("safe", 1, 1, false, false, true, 0)).issueCount());
        assertThrows(UnsupportedOperationException.class, () -> old.findings().clear());
    }

    @Test
    void scoreIsClampedAtZeroAndCriticalMeansOnlyCriticalSeverity() {
        SecurityReport report = new SecurityAuditVisitor().analyze(FintechProject.create());
        assertEquals(0, report.score());
        assertEquals(1, report.criticalIssueCount());
        assertTrue(Severity.CRITICAL.blocksRelease());
        assertFalse(Severity.HIGH.blocksRelease());
        assertFalse(Severity.MEDIUM.blocksRelease());
        assertFalse(Severity.LOW.blocksRelease());
    }
}

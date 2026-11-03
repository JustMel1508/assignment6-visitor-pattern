package com.example.codeinspection;

import com.example.codeinspection.demo.FintechProject;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import com.example.codeinspection.report.QualityGateReport;
import com.example.codeinspection.visitor.ProjectStatisticsVisitor;
import com.example.codeinspection.visitor.QualityGateVisitor;
import com.example.codeinspection.visitor.SecurityAuditVisitor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QualityGateTest {
    private static Directory project(int sourceCount, int testCount, int complexity, boolean credentials) {
        Directory root = new Directory("root");
        for (int i = 0; i < sourceCount; i++) {
            root.add(new SourceCodeFile("S" + i, 1, complexity, credentials, false, true, 0));
        }
        for (int i = 0; i < testCount; i++) {
            root.add(new TestFile("T" + i, 1, "S" + i));
        }
        return root;
    }

    @Test
    void exactComplexityAndRatioBoundariesPassEveryGate() {
        QualityGateReport report = new QualityGateVisitor().analyze(project(5, 2, 10, false));
        assertEquals(10.0, report.statistics().averageComplexity());
        assertEquals(0.40, report.statistics().testSourceRatio());
        assertTrue(report.securityPassed());
        assertTrue(report.complexityPassed());
        assertTrue(report.testsPassed());
        assertTrue(report.passed());
    }

    @Test
    void excessiveComplexityFailsIndependently() {
        QualityGateReport report = new QualityGateVisitor().analyze(project(1, 1, 11, false));
        assertFalse(report.complexityPassed());
        assertTrue(report.securityPassed());
        assertTrue(report.testsPassed());
        assertFalse(report.passed());
    }

    @Test
    void insufficientTestsFailIndependently() {
        QualityGateReport report = new QualityGateVisitor().analyze(project(3, 1, 5, false));
        assertTrue(report.securityPassed());
        assertTrue(report.complexityPassed());
        assertFalse(report.testsPassed());
        assertFalse(report.passed());
    }

    @Test
    void criticalFindingFailsEvenWhenOtherGatesPass() {
        QualityGateReport report = new QualityGateVisitor().analyze(project(1, 1, 5, true));
        assertEquals(1, report.security().criticalIssueCount());
        assertFalse(report.securityPassed());
        assertTrue(report.complexityPassed());
        assertTrue(report.testsPassed());
        assertFalse(report.passed());
    }

    @Test
    void emptyTestOnlyAndNonproductionProjectsCannotAppearReady() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        assertTrue(visitor.analyze(project(1, 1, 5, false)).passed());
        QualityGateReport empty = visitor.analyze(new Directory("empty"));
        assertTrue(empty.securityPassed());
        assertTrue(empty.complexityPassed());
        assertFalse(empty.testsPassed());
        assertFalse(empty.passed());
        assertFalse(visitor.analyze(new TestFile("T", 1, "unknown")).passed());
        assertFalse(visitor.analyze(new Directory("tools")
                .add(new SourceCodeFile("Tool", 1, 1, false, false, false, 0))
                .add(new TestFile("ToolTest", 1, "Tool"))).passed());
    }

    @Test
    void reportsUseIdenticalSecurityAndStatisticsRulesWithoutDoubleTraversal() {
        Directory root = FintechProject.create();
        QualityGateReport report = new QualityGateVisitor().analyze(root);
        assertEquals(new SecurityAuditVisitor().analyze(root), report.security());
        assertEquals(new ProjectStatisticsVisitor().analyze(root), report.statistics());
        assertFalse(report.passed());
        assertTrue(report.complexityPassed());
        assertTrue(report.testsPassed());
    }

    @Test
    void highSeverityAloneDoesNotCountAsCritical() {
        Directory root = project(1, 1, 5, false)
                .add(new SourceCodeFile("Risk", 1, 5, false, true, true, 0));
        QualityGateReport report = new QualityGateVisitor().analyze(root);
        assertEquals(1, report.security().issueCount());
        assertEquals(0, report.security().criticalIssueCount());
        assertTrue(report.passed());
    }
}

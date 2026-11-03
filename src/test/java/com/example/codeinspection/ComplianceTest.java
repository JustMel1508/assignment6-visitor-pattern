package com.example.codeinspection;

import com.example.codeinspection.demo.FintechProject;
import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.Dependency;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import com.example.codeinspection.report.ComplianceReport;
import com.example.codeinspection.report.ComplianceRule;
import com.example.codeinspection.visitor.ComplianceVisitor;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComplianceTest {
    @Test
    void nullEmptyAndWhitespaceVersionsEachIdentifyTheDependency() {
        ComplianceReport report = new ComplianceVisitor().analyze(new DependencyFile("pom.xml", List.of(
                new Dependency("null-version", null, false), new Dependency("empty-version", "", false),
                new Dependency("blank-version", " \t ", false), new Dependency("valid", " 1.0 ", false))));
        assertEquals(3, report.violationCount());
        assertEquals(List.of("pom.xml -> null-version", "pom.xml -> empty-version", "pom.xml -> blank-version"),
                report.violations().stream().map(v -> v.element()).toList());
        assertFalse(report.passed(ComplianceRule.EXPLICIT_DEPENDENCY_VERSIONS));
        assertTrue(report.passed(ComplianceRule.NO_PROHIBITED_DEPENDENCIES));
    }

    @Test
    void prohibitedIdentifiersMatchExactlyAndAllowedEntriesRemainValid() {
        ComplianceReport report = new ComplianceVisitor().analyze(new DependencyFile("pom.xml", List.of(
                new Dependency("org.example:unsafe-crypto", "1", false),
                new Dependency("org.example:unlicensed-sdk", "1", false),
                new Dependency("org.example:payment-sdk", "1", false),
                new Dependency("org.example:unsafe-crypto-extra", "1", false))));
        assertEquals(2, report.violationCount());
        assertTrue(report.violations().stream().allMatch(v -> v.rule() == ComplianceRule.NO_PROHIBITED_DEPENDENCIES));
        assertFalse(report.compliant());
    }

    @Test
    void testMatchingWorksAcrossDirectoriesInEitherTraversalOrder() {
        SourceCodeFile pay = new SourceCodeFile("Pay", 1, 1, false, false, true, 0);
        SourceCodeFile user = new SourceCodeFile("User", 1, 1, false, false, true, 0);
        SourceCodeFile tool = new SourceCodeFile("Tool", 1, 1, false, false, false, 0);
        TestFile test = new TestFile("PayTest", 1, "Pay");
        Directory sourcesFirst = new Directory("root")
                .add(new Directory("src").add(pay).add(user).add(tool))
                .add(new Directory("tests").add(test));
        Directory testsFirst = new Directory("root")
                .add(new Directory("tests").add(test))
                .add(new Directory("src").add(pay).add(user).add(tool));
        ComplianceReport report = new ComplianceVisitor().analyze(sourcesFirst);
        assertEquals(new ComplianceVisitor().analyze(testsFirst), report);
        assertEquals(1, report.violationCount());
        assertEquals("User", report.violations().get(0).element());
        assertEquals(ComplianceRule.PRODUCTION_MODULES_TESTED, report.violations().get(0).rule());
    }

    @Test
    void credentialsInSourceAndConfigurationBothViolatePolicy() {
        ComplianceReport report = new ComplianceVisitor().analyze(new Directory("root")
                .add(new SourceCodeFile("S", 1, 1, true, false, false, 0))
                .add(new ConfigurationFile("config", false, false, false, true)));
        assertEquals(2, report.violationCount());
        assertTrue(report.violations().stream().allMatch(v -> v.rule() == ComplianceRule.NO_EXPOSED_CREDENTIALS));
    }

    @Test
    void compliantProjectAndEmptyProjectPassWithoutStaleOrMutableResults() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        ComplianceReport demo = visitor.analyze(FintechProject.create());
        assertEquals(4, demo.violationCount());
        Directory clean = new Directory("root")
                .add(new SourceCodeFile("S", 1, 1, false, false, true, 0))
                .add(new TestFile("STest", 1, "S"))
                .add(new DependencyFile("pom", List.of(new Dependency("allowed", "1", false))));
        assertTrue(visitor.analyze(clean).compliant());
        assertEquals(visitor.report(), visitor.report());
        assertTrue(visitor.analyze(new Directory("empty")).compliant());
        assertEquals(4, demo.violationCount());
        assertThrows(UnsupportedOperationException.class, () -> demo.violations().clear());
    }

    @Test
    void snapshotsDoNotFinalizeEarlyAndMatchingIsCaseSensitive() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new SourceCodeFile("Pay", 1, 1, false, false, true, 0).accept(visitor);
        ComplianceReport before = visitor.report();
        new TestFile("WrongCase", 1, "pay").accept(visitor);
        assertEquals(1, visitor.report().violationCount());
        new TestFile("Correct", 1, "Pay").accept(visitor);
        assertTrue(visitor.report().compliant());
        assertEquals(1, before.violationCount());
    }

    @Test
    void ambiguousBasenamesCannotFalselySatisfyProductionTests() {
        ComplianceReport report = new ComplianceVisitor().analyze(new Directory("root")
                .add(new Directory("a").add(new SourceCodeFile("Service", 1, 1, false, false, true, 0)))
                .add(new Directory("b").add(new SourceCodeFile("Service", 2, 2, false, false, true, 0)))
                .add(new TestFile("Test", 1, "Service")));
        assertEquals(2, report.violationCount());
        assertTrue(report.violations().stream().allMatch(v -> v.explanation().contains("ambiguous")));
    }
}

package com.example.codeinspection;

import com.example.codeinspection.demo.FintechProject;
import com.example.codeinspection.model.Dependency;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import com.example.codeinspection.report.StatisticsReport;
import com.example.codeinspection.visitor.ProjectStatisticsVisitor;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatisticsTest {
    @Test
    void fintechMetricsCountFilesEntriesAndCodeLinesExactlyOnce() {
        StatisticsReport report = new ProjectStatisticsVisitor().analyze(FintechProject.create());
        assertEquals(5, report.fileCount());
        assertEquals(390, report.linesOfCode());
        assertEquals(1, report.testFileCount());
        assertEquals(3, report.dependencyCount());
        assertEquals(2, report.sourceFileCount());
        assertEquals(2, report.productionSourceCount());
        assertEquals(4, report.directoryCount());
        assertEquals(9.0, report.averageComplexity());
        assertEquals(0.5, report.testSourceRatio());
    }

    @Test
    void severalManifestsAndFractionalMetricsUseCorrectDenominators() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("A", 1, 1, false, false, true, 0))
                .add(new SourceCodeFile("B", 2, 2, false, false, true, 0))
                .add(new SourceCodeFile("C", 3, 2, false, false, false, 0))
                .add(new TestFile("ATest", 4, "A"))
                .add(new DependencyFile("pom.xml", List.of(new Dependency("a", "1", false))))
                .add(new Directory("module").add(new DependencyFile("module/pom.xml", List.of(
                        new Dependency("b", "1", false), new Dependency("c", "1", false)))));
        StatisticsReport report = new ProjectStatisticsVisitor().analyze(root);
        assertEquals(6, report.fileCount());
        assertEquals(10, report.linesOfCode());
        assertEquals(3, report.dependencyCount());
        assertEquals(2, report.productionSourceCount());
        assertEquals(5.0 / 3.0, report.averageComplexity(), 1e-12);
        assertEquals(1.0 / 3.0, report.testSourceRatio(), 1e-12);
    }

    @Test
    void emptyAndTestOnlyProjectsReturnFiniteZeroRatiosAndResetState() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        StatisticsReport original = visitor.analyze(FintechProject.create());
        StatisticsReport empty = visitor.analyze(new Directory("empty"));
        assertEquals(0, empty.fileCount());
        assertEquals(1, empty.directoryCount());
        assertEquals(0.0, empty.averageComplexity());
        assertEquals(0.0, empty.testSourceRatio());
        StatisticsReport testOnly = visitor.analyze(new TestFile("test", 2, "missing"));
        assertEquals(0.0, testOnly.testSourceRatio());
        assertEquals(0.0, testOnly.averageComplexity());
        assertEquals(2, testOnly.linesOfCode());
        assertEquals(5, original.fileCount());
    }

    @Test
    void aggregateCountersDoNotOverflowAtIntegerBoundaries() {
        Directory root = new Directory("root")
                .add(new SourceCodeFile("a", Integer.MAX_VALUE, Integer.MAX_VALUE, false, false, true, 0))
                .add(new SourceCodeFile("b", Integer.MAX_VALUE, Integer.MAX_VALUE, false, false, true, 0));
        StatisticsReport report = new ProjectStatisticsVisitor().analyze(root);
        assertEquals(4294967294L, report.linesOfCode());
        assertEquals(2147483647.0, report.averageComplexity());
    }
}

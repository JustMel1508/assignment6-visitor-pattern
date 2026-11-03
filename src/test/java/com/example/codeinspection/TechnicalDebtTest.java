package com.example.codeinspection;

import com.example.codeinspection.demo.FintechProject;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.report.TechnicalDebtEntry;
import com.example.codeinspection.report.TechnicalDebtReport;
import com.example.codeinspection.visitor.TechnicalDebtVisitor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TechnicalDebtTest {
    @Test
    void fintechDebtIsItemizedAndCalculatedFromEveryIndicator() {
        TechnicalDebtReport report = new TechnicalDebtVisitor().analyze(FintechProject.create());
        assertEquals(2, report.findings().size());
        assertEquals(360, report.estimatedMinutes());
        assertEquals(new TechnicalDebtEntry("PaymentService.java", 90, 120, 120), report.perFile().get(0));
        assertEquals(330, report.perFile().get(0).estimatedMinutes());
        assertEquals(new TechnicalDebtEntry("UserService.java", 30, 0, 0), report.perFile().get(1));
    }

    @Test
    void zeroDebtAtComplexityBoundaryAndEmptyFilesDoNotGenerateFindings() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        TechnicalDebtReport clean = visitor.analyze(new SourceCodeFile("Clean", 0, 10, false, false, true, 0));
        assertEquals(0, clean.estimatedMinutes());
        assertTrue(clean.findings().isEmpty());
        assertEquals(1, clean.perFile().size());
        TechnicalDebtReport empty = visitor.analyze(new Directory("empty"));
        assertEquals(0, empty.estimatedMinutes());
        assertTrue(empty.perFile().isEmpty());
        assertTrue(empty.findings().isEmpty());
    }

    @Test
    void repeatedAnalysisIsDeterministicAndReportsRemainImmutable() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        TechnicalDebtReport first = visitor.analyze(FintechProject.create());
        assertEquals(first, visitor.analyze(FintechProject.create()));
        visitor.reset();
        assertEquals(0, visitor.report().estimatedMinutes());
        assertEquals(360, first.estimatedMinutes());
        assertThrows(UnsupportedOperationException.class, () -> first.perFile().clear());
        assertThrows(UnsupportedOperationException.class, () -> first.findings().clear());
    }

    @Test
    void debtMultiplicationUsesLongArithmetic() {
        TechnicalDebtReport report = new TechnicalDebtVisitor().analyze(new SourceCodeFile(
                "Large", 1, Integer.MAX_VALUE, false, true, true, Integer.MAX_VALUE));
        assertEquals(193273527750L, report.estimatedMinutes());
    }
}

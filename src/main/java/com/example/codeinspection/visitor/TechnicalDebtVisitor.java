package com.example.codeinspection.visitor;

import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.ProjectElement;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import com.example.codeinspection.report.TechnicalDebtEntry;
import com.example.codeinspection.report.TechnicalDebtReport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** An independent operation added using the existing element and visitor contracts. */
public final class TechnicalDebtVisitor implements ProjectVisitor {
    private static final int TODO_MINUTES = 30;
    private static final int COMPLEXITY_BASELINE = 10;
    private static final int EXCESS_COMPLEXITY_MINUTES = 60;
    private static final int HIGH_RISK_MINUTES = 120;
    private final List<TechnicalDebtEntry> perFile = new ArrayList<>();

    public TechnicalDebtReport analyze(ProjectElement root) {
        Objects.requireNonNull(root, "root");
        reset();
        root.accept(this);
        return report();
    }

    public void reset() {
        perFile.clear();
    }

    public TechnicalDebtReport report() {
        return new TechnicalDebtReport(perFile);
    }

    @Override
    public void visit(SourceCodeFile file) {
        perFile.add(new TechnicalDebtEntry(file.name(), (long) file.todoCount() * TODO_MINUTES,
                Math.max(0L, (long) file.complexity() - COMPLEXITY_BASELINE) * EXCESS_COMPLEXITY_MINUTES,
                file.highRisk() ? HIGH_RISK_MINUTES : 0));
    }

    @Override public void visit(TestFile file) { }
    @Override public void visit(ConfigurationFile file) { }
    @Override public void visit(DependencyFile file) { }
    @Override public void visit(Directory directory) { }
}

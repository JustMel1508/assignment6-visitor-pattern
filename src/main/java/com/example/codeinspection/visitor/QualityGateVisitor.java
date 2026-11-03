package com.example.codeinspection.visitor;

import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.ProjectElement;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import com.example.codeinspection.report.QualityGateReport;
import java.util.Objects;

/** Delegates each dispatched element to the same calculators used in standalone reports. */
public final class QualityGateVisitor implements ProjectVisitor {
    private final SecurityAuditVisitor security = new SecurityAuditVisitor();
    private final ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();

    public QualityGateReport analyze(ProjectElement root) {
        Objects.requireNonNull(root, "root");
        reset();
        root.accept(this);
        return report();
    }

    public void reset() {
        security.reset();
        statistics.reset();
    }

    public QualityGateReport report() {
        return new QualityGateReport(security.report(), statistics.report());
    }

    @Override
    public void visit(SourceCodeFile file) {
        security.visit(file);
        statistics.visit(file);
    }

    @Override
    public void visit(TestFile file) {
        security.visit(file);
        statistics.visit(file);
    }

    @Override
    public void visit(ConfigurationFile file) {
        security.visit(file);
        statistics.visit(file);
    }

    @Override
    public void visit(DependencyFile file) {
        security.visit(file);
        statistics.visit(file);
    }

    @Override
    public void visit(Directory directory) {
        security.visit(directory);
        statistics.visit(directory);
    }
}

package com.example.codeinspection.visitor;

import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.ProjectElement;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import com.example.codeinspection.report.StatisticsReport;
import java.util.Objects;

public final class ProjectStatisticsVisitor implements ProjectVisitor {
    private long files;
    private long linesOfCode;
    private long tests;
    private long dependencies;
    private long sources;
    private long productionSources;
    private long directories;
    private long complexitySum;

    public StatisticsReport analyze(ProjectElement root) {
        Objects.requireNonNull(root, "root");
        reset();
        root.accept(this);
        return report();
    }

    public void reset() {
        files = linesOfCode = tests = dependencies = sources = productionSources = directories = complexitySum = 0;
    }

    public StatisticsReport report() {
        return new StatisticsReport(files, linesOfCode, tests, dependencies, sources,
                productionSources, directories, complexitySum);
    }

    @Override
    public void visit(SourceCodeFile file) {
        files++;
        sources++;
        linesOfCode += file.linesOfCode();
        complexitySum += file.complexity();
        if (file.production()) {
            productionSources++;
        }
    }

    @Override
    public void visit(TestFile file) {
        files++;
        tests++;
        linesOfCode += file.linesOfCode();
    }

    @Override public void visit(ConfigurationFile file) { files++; }

    @Override
    public void visit(DependencyFile file) {
        files++;
        dependencies += file.dependencies().size();
    }

    @Override public void visit(Directory directory) { directories++; }
}

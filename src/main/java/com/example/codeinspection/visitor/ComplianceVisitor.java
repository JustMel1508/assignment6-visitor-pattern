package com.example.codeinspection.visitor;

import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.Dependency;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.ProjectElement;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import com.example.codeinspection.report.ComplianceReport;
import com.example.codeinspection.report.ComplianceRule;
import com.example.codeinspection.report.ComplianceViolation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ComplianceVisitor implements ProjectVisitor {
    public static final Set<String> PROHIBITED_DEPENDENCIES = Set.of(
            "org.example:unsafe-crypto", "org.example:unlicensed-sdk");

    private final List<ComplianceViolation> violations = new ArrayList<>();
    private final List<SourceCodeFile> productionSources = new ArrayList<>();
    private final Map<String, Integer> sourceNameCounts = new HashMap<>();
    private final Set<String> testedSources = new HashSet<>();

    public ComplianceReport analyze(ProjectElement root) {
        Objects.requireNonNull(root, "root");
        reset();
        root.accept(this);
        return report();
    }

    public void reset() {
        violations.clear();
        productionSources.clear();
        sourceNameCounts.clear();
        testedSources.clear();
    }

    /** Cross-file rules are evaluated on a copy, so reports never mutate traversal state. */
    public ComplianceReport report() {
        List<ComplianceViolation> complete = new ArrayList<>(violations);
        for (SourceCodeFile source : productionSources) {
            boolean ambiguous = sourceNameCounts.get(source.name()) > 1;
            if (ambiguous || !testedSources.contains(source.name())) {
                String explanation = ambiguous
                        ? "Source identifier is ambiguous; use unique project-relative names for test matching."
                        : "Production source has no test with testedSource exactly equal to " + source.name() + ".";
                complete.add(new ComplianceViolation(source.name(),
                        ComplianceRule.PRODUCTION_MODULES_TESTED, explanation));
            }
        }
        return new ComplianceReport(complete);
    }

    @Override
    public void visit(SourceCodeFile file) {
        sourceNameCounts.merge(file.name(), 1, Integer::sum);
        if (file.production()) {
            productionSources.add(file);
        }
        if (file.hardcodedCredentials()) {
            violations.add(new ComplianceViolation(file.name(), ComplianceRule.NO_EXPOSED_CREDENTIALS,
                    "Source metadata indicates exposed hardcoded credentials."));
        }
    }

    @Override
    public void visit(TestFile file) {
        testedSources.add(file.testedSource());
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.exposedCredentials()) {
            violations.add(new ComplianceViolation(file.name(), ComplianceRule.NO_EXPOSED_CREDENTIALS,
                    "Configuration metadata indicates exposed credentials."));
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.dependencies()) {
            String element = file.name() + " -> " + dependency.identifier();
            if (!dependency.hasVersion()) {
                violations.add(new ComplianceViolation(element, ComplianceRule.EXPLICIT_DEPENDENCY_VERSIONS,
                        "Dependency requires an explicit nonblank version."));
            }
            if (PROHIBITED_DEPENDENCIES.contains(dependency.identifier())) {
                violations.add(new ComplianceViolation(element, ComplianceRule.NO_PROHIBITED_DEPENDENCIES,
                        "Dependency identifier is on the project's prohibited list."));
            }
        }
    }

    @Override public void visit(Directory directory) { }
}

package com.example.codeinspection.visitor;

import com.example.codeinspection.model.ConfigurationFile;
import com.example.codeinspection.model.Dependency;
import com.example.codeinspection.model.DependencyFile;
import com.example.codeinspection.model.Directory;
import com.example.codeinspection.model.ProjectElement;
import com.example.codeinspection.model.SourceCodeFile;
import com.example.codeinspection.model.TestFile;
import com.example.codeinspection.report.SecurityFinding;
import com.example.codeinspection.report.SecurityReport;
import com.example.codeinspection.report.Severity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SecurityAuditVisitor implements ProjectVisitor {
    private final List<SecurityFinding> findings = new ArrayList<>();

    public SecurityReport analyze(ProjectElement root) {
        Objects.requireNonNull(root, "root");
        reset();
        root.accept(this);
        return report();
    }

    public void reset() {
        findings.clear();
    }

    public SecurityReport report() {
        return new SecurityReport(findings);
    }

    @Override
    public void visit(SourceCodeFile file) {
        if (file.hardcodedCredentials()) {
            add(file.name(), "HARDCODED_CREDENTIALS", Severity.CRITICAL,
                    "Source metadata indicates hardcoded credentials; use a secret provider.");
        }
        if (file.highRisk()) {
            add(file.name(), "HIGH_RISK_SOURCE", Severity.HIGH,
                    "Source is flagged high risk and requires security review.");
        }
    }

    @Override public void visit(TestFile file) { }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.exposedCredentials()) {
            add(file.name(), "EXPOSED_CREDENTIALS", Severity.CRITICAL,
                    "Configuration metadata indicates exposed credentials.");
        }
        if (file.production() && file.debugEnabled()) {
            add(file.name(), "PRODUCTION_DEBUG", Severity.HIGH,
                    "Debug mode is enabled in a production configuration.");
        }
        if (file.insecureSettings()) {
            add(file.name(), "INSECURE_CONFIGURATION", Severity.MEDIUM,
                    "Configuration contains insecure settings, such as disabled TLS validation.");
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.dependencies()) {
            if (dependency.knownVulnerability()) {
                add(file.name() + " -> " + dependency.identifier(), "VULNERABLE_DEPENDENCY", Severity.HIGH,
                        "Version " + (dependency.hasVersion() ? dependency.version() : "<unspecified>")
                                + " is flagged vulnerable in supplied metadata (no live scan).");
            }
        }
    }

    @Override public void visit(Directory directory) { }

    private void add(String element, String rule, Severity severity, String explanation) {
        findings.add(new SecurityFinding(element, rule, severity, explanation));
    }
}

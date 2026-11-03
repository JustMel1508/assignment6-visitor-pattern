package com.example.codeinspection.report;

import java.util.List;

public record ComplianceReport(List<ComplianceViolation> violations) {
    public ComplianceReport {
        violations = List.copyOf(violations);
    }

    public int violationCount() {
        return violations.size();
    }

    public boolean compliant() {
        return violations.isEmpty();
    }

    public boolean passed(ComplianceRule rule) {
        return violations.stream().noneMatch(v -> v.rule() == rule);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("COMPLIANCE REPORT\n");
        for (ComplianceRule rule : ComplianceRule.values()) {
            result.append(rule).append(": ").append(passed(rule) ? "PASS" : "FAIL").append('\n');
        }
        result.append("Violations: ").append(violationCount()).append('\n');
        for (ComplianceViolation violation : violations) {
            result.append(violation).append('\n');
        }
        return result.append("OVERALL: ").append(compliant() ? "COMPLIANT" : "NON-COMPLIANT").toString();
    }
}

package com.example.codeinspection.report;

import java.util.List;
import java.util.stream.Collectors;

public record SecurityReport(List<SecurityFinding> findings) {
    public SecurityReport {
        findings = List.copyOf(findings);
    }

    public int issueCount() {
        return findings.size();
    }

    public long criticalIssueCount() {
        return findings.stream().filter(f -> f.severity().blocksRelease()).count();
    }

    public int score() {
        long penalty = findings.stream().mapToLong(f -> f.severity().scorePenalty()).sum();
        return (int) Math.max(0L, 100L - penalty);
    }

    @Override
    public String toString() {
        String details = findings.isEmpty() ? "No security findings."
                : findings.stream().map(SecurityFinding::toString).collect(Collectors.joining("\n"));
        return "SECURITY AUDIT REPORT\nIssues: " + issueCount() + " | Critical: " + criticalIssueCount()
                + " | Security score: " + score() + "/100\n" + details;
    }
}

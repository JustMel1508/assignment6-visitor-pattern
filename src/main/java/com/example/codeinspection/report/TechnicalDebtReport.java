package com.example.codeinspection.report;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public record TechnicalDebtReport(List<TechnicalDebtEntry> perFile) {
    public TechnicalDebtReport {
        perFile = List.copyOf(perFile);
    }

    /** Each affected source file is one finding with an itemized effort estimate. */
    public List<TechnicalDebtEntry> findings() {
        return perFile.stream().filter(entry -> entry.estimatedMinutes() > 0).toList();
    }

    public long estimatedMinutes() {
        return perFile.stream().mapToLong(TechnicalDebtEntry::estimatedMinutes).sum();
    }

    @Override
    public String toString() {
        String details = perFile.isEmpty() ? "No source files; no technical debt findings."
                : perFile.stream().map(TechnicalDebtEntry::toString).collect(Collectors.joining("\n"));
        return String.format(Locale.ROOT,
                "TECHNICAL DEBT REPORT%nFindings (affected source files): %d%n"
                        + "Estimated debt: %d min (%.2f hours)%n%s",
                findings().size(), estimatedMinutes(), estimatedMinutes() / 60.0, details);
    }
}

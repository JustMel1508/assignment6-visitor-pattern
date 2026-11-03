package com.example.codeinspection.report;

/** One source file's estimated remediation effort, in minutes. */
public record TechnicalDebtEntry(String file, long todoMinutes, long complexityMinutes, long riskMinutes) {
    public long estimatedMinutes() {
        return todoMinutes + complexityMinutes + riskMinutes;
    }

    @Override
    public String toString() {
        return file + ": " + estimatedMinutes() + " min (TODO/FIXME: " + todoMinutes
                + "; excess complexity: " + complexityMinutes + "; high risk: " + riskMinutes + ")";
    }
}

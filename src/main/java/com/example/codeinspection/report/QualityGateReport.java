package com.example.codeinspection.report;

import java.util.Locale;

public record QualityGateReport(SecurityReport security, StatisticsReport statistics) {
    public static final double MAX_AVERAGE_COMPLEXITY = 10.0;
    public static final double MIN_TEST_SOURCE_RATIO = 0.40;

    public boolean securityPassed() {
        return security.criticalIssueCount() == 0;
    }

    public boolean complexityPassed() {
        return statistics.averageComplexity() <= MAX_AVERAGE_COMPLEXITY;
    }

    public boolean testsPassed() {
        return statistics.productionSourceCount() > 0
                && statistics.testSourceRatio() >= MIN_TEST_SOURCE_RATIO;
    }

    public boolean passed() {
        return securityPassed() && complexityPassed() && testsPassed();
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "QUALITY GATE REPORT%nSecurity:   %s (critical issues: %d; required: 0)%n"
                        + "Complexity: %s (average: %.2f; maximum: %.2f)%n"
                        + "Tests:      %s (test/source ratio: %.2f; minimum: %.2f; production sources: %d)%n"
                        + "Production source required for release; test/source ratio is not measured coverage.%n"
                        + "FINAL RESULT: %s",
                status(securityPassed()), security.criticalIssueCount(), status(complexityPassed()),
                statistics.averageComplexity(), MAX_AVERAGE_COMPLEXITY, status(testsPassed()),
                statistics.testSourceRatio(), MIN_TEST_SOURCE_RATIO, statistics.productionSourceCount(),
                passed() ? "PASS" : "FAILED");
    }

    private static String status(boolean passed) {
        return passed ? "PASS" : "FAIL";
    }
}

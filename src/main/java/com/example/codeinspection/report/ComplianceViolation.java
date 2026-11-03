package com.example.codeinspection.report;

public record ComplianceViolation(String element, ComplianceRule rule, String explanation) {
    @Override
    public String toString() {
        return element + " | " + rule + ": " + explanation;
    }
}
